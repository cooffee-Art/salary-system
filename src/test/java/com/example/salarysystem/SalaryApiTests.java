package com.example.salarysystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Sql("/employee-test.sql")
class SalaryApiTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbc;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void pageJoinsEmployeeAndDepartment() throws Exception {
        mvc.perform(get("/api/salaries/page").param("month", "2024-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].empNo").value("E001"))
                .andExpect(jsonPath("$.data.records[0].empName").value("张三"))
                .andExpect(jsonPath("$.data.records[0].deptName").value("技术部"))
                .andExpect(jsonPath("$.data.records[0].actualPay").value(8000.0));
    }

    @Test
    void departmentReportAggregatesByMonth() throws Exception {
        mvc.perform(get("/api/salaries/report/department").param("month", "2024-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].deptName").value("技术部"))
                .andExpect(jsonPath("$.data[0].empCount").value(1))
                .andExpect(jsonPath("$.data[0].totalActual").value(8000.0));
    }

    @Test
    void generateUsesLatestBasePayAndIsIdempotent() throws Exception {
        mvc.perform(post("/api/salaries/generate").param("month", "2024-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.generated").value(1))
                .andExpect(jsonPath("$.data.skipped").value(1))
                .andExpect(jsonPath("$.data.totalActual").value(8000.0));

        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_salary WHERE salary_month='2024-10'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT actual_pay FROM sys_salary WHERE salary_month='2024-10'", java.math.BigDecimal.class))
                .isEqualByComparingTo("8000.00");

        mvc.perform(post("/api/salaries/generate").param("month", "2024-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("该月工资已生成，请勿重复操作"));
    }

    @Test
    void invalidMonthIsRejected() throws Exception {
        mvc.perform(get("/api/salaries/report/department").param("month", "2024-13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
        mvc.perform(post("/api/salaries/generate").param("month", "2999-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
