package com.example.salarysystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 通过真实 Controller、校验器、Service、Mapper 和 H2 数据库验证薪资接口契约。 */
@SpringBootTest
@ActiveProfiles("test")
@Sql("/salary-test.sql")
class SalaryApiTests {

    @Autowired private WebApplicationContext context;
    @Autowired private JdbcTemplate jdbc;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private int salaryCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM sys_salary", Integer.class);
    }

    @Test
    void createRecalculatesActualPayAndIsQueryable() throws Exception {
        mvc.perform(post("/api/salaries").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empId":1,"salaryMonth":"2024-10","basePay":6000,"bonus":100,"deduction":100}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.actualPay").value(6000.00));

        mvc.perform(get("/api/salaries").param("salaryMonth", "2024-10"))
                .andExpect(jsonPath("$.data[0].empName").value("张三"))
                .andExpect(jsonPath("$.data[0].actualPay").value(6000.00));
    }

    @Test
    void duplicateMonthFailsWithoutInserting() throws Exception {
        mvc.perform(post("/api/salaries").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empId":1,"salaryMonth":"2024-09","basePay":1000,"bonus":0,"deduction":0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("已存在薪资记录")));
        assertThat(salaryCount()).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"empId\":1,\"salaryMonth\":\"2024-10\",\"basePay\":-100}",
            "{\"empId\":1,\"salaryMonth\":\"2024-10\",\"basePay\":1000,\"bonus\":0,\"deduction\":2000}",
            "{\"empId\":1,\"salaryMonth\":\"2024/10\",\"basePay\":1000}",
            "{\"empId\":999,\"salaryMonth\":\"2024-10\",\"basePay\":1000}",
            "{\"empId\":1,\"salaryMonth\":\"2024-10\",\"basePay\":1000,\"bonus\":-50}",
            "{broken"
    })
    void invalidCreateReturnsBusiness400AndDoesNotWrite(String body) throws Exception {
        mvc.perform(post("/api/salaries").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").value(nullValue()));
        assertThat(salaryCount()).isEqualTo(3);
    }

    @Test
    void updateRecalculatesActualPay() throws Exception {
        mvc.perform(put("/api/salaries/1").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empId":1,"salaryMonth":"2024-09","basePay":8000,"bonus":2000,"deduction":1000}
                                """))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.actualPay").value(9000.00));

        mvc.perform(get("/api/salaries/1"))
                .andExpect(jsonPath("$.data.actualPay").value(9000.00));
    }

    @Test
    void updateDuplicateMonthDoesNotChangeRecord() throws Exception {
        mvc.perform(put("/api/salaries/2").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"empId":1,"salaryMonth":"2024-09","basePay":7000,"bonus":500,"deduction":0}
                                """))
                .andExpect(jsonPath("$.code").value(400));
        assertThat(jdbc.queryForObject("SELECT actual_pay FROM sys_salary WHERE id=2", java.math.BigDecimal.class))
                .isEqualByComparingTo("7500.00");
    }

    @Test
    void missingSalaryReturns404ForReadUpdateAndDelete() throws Exception {
        mvc.perform(get("/api/salaries/999")).andExpect(jsonPath("$.code").value(404));
        mvc.perform(put("/api/salaries/999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empId\":1,\"salaryMonth\":\"2024-10\",\"basePay\":1000}"))
                .andExpect(jsonPath("$.code").value(404));
        mvc.perform(delete("/api/salaries/999")).andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void deleteRemovesRecord() throws Exception {
        mvc.perform(delete("/api/salaries/3")).andExpect(jsonPath("$.code").value(200));
        mvc.perform(get("/api/salaries/3")).andExpect(jsonPath("$.code").value(404));
        assertThat(salaryCount()).isEqualTo(2);
    }

    @Test
    void listFiltersByMonthAndEmployee() throws Exception {
        mvc.perform(get("/api/salaries").param("salaryMonth", "2024-09"))
                .andExpect(jsonPath("$.data", hasSize(3)));
        mvc.perform(get("/api/salaries").param("empId", "1"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].empNo").value("E001"));
    }

    @Test
    void pageAppliesBoundaryDefaults() throws Exception {
        mvc.perform(get("/api/salaries/page").param("current", "0").param("size", "1000"))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(100))
                .andExpect(jsonPath("$.data.total").value(3));
    }

    @Test
    void referenceToMissingEmployeeReturns400() throws Exception {
        mvc.perform(post("/api/salaries").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empId\":999,\"salaryMonth\":\"2024-10\",\"basePay\":1000}"))
                .andExpect(jsonPath("$.code").value(400));
        assertThat(salaryCount()).isEqualTo(3);
    }
}
