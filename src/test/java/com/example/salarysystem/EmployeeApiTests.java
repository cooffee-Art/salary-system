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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 通过真实 Controller、校验器、Service、Mapper 和数据库验证接口契约。 */
@SpringBootTest
@ActiveProfiles("test")
@Sql("/employee-test.sql")
class EmployeeApiTests {
    @Autowired private WebApplicationContext context;
    @Autowired private JdbcTemplate jdbc;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void createReturnsIdAndDefaultsAndIsQueryable() throws Exception {
        create("""
                {"empNo":" E004 ","name":" 赵六 ","phone":"","deptId":1,"hireDate":"2024-05-10"}
                """)
                .andExpect(jsonPath("$.data.id", greaterThan(3)))
                .andExpect(jsonPath("$.data.empNo").value("E004"))
                .andExpect(jsonPath("$.data.name").value("赵六"))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.phone").value(nullValue()));
        mvc.perform(get("/api/employees/page").param("keyword", "E004"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].deptName").value("技术部"));
    }

    @Test
    void duplicateNumberFailsWithoutInserting() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empNo\":\"E001\",\"name\":\"重复\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("工号已存在"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_employee", Integer.class)).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}", "{\"empNo\":\" \",\"name\":\"名字\"}",
            "{\"empNo\":\"E004\",\"name\":\" \"}",
            "{\"empNo\":\"123456789012345678901\",\"name\":\"名字\"}",
            "{\"empNo\":\"E004\",\"name\":\"名字\",\"phone\":\"123\"}",
            "{\"empNo\":\"E004\",\"name\":\"名字\",\"gender\":\"其他\"}",
            "{\"empNo\":\"E004\",\"name\":\"名字\",\"status\":2}",
            "{\"empNo\":\"E004\",\"name\":\"名字\",\"deptId\":0}",
            "{\"empNo\":\"E004\",\"name\":\"名字\",\"deptId\":999}",
            "{\"empNo\":\"E004\",\"name\":\"名字\",\"hireDate\":\"2999-01-01\"}",
            "{\"empNo\":\"E004\",\"name\":\"名字\",\"hireDate\":\"bad-date\"}",
            "{broken", "null"
    })
    void invalidCreateReturnsBusiness400AndDoesNotWrite(String body) throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").value(nullValue()));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_employee", Integer.class)).isEqualTo(3);
    }

    @Test
    void updateCanClearOptionalFieldsAndKeepOwnNumber() throws Exception {
        mvc.perform(put("/api/employees/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empNo\":\"E001\",\"name\":\"新姓名\",\"status\":0}"))
                .andExpect(jsonPath("$.code").value(200));
        mvc.perform(get("/api/employees/1"))
                .andExpect(jsonPath("$.data.name").value("新姓名"))
                .andExpect(jsonPath("$.data.deptId").value(nullValue()))
                .andExpect(jsonPath("$.data.phone").value(nullValue()))
                .andExpect(jsonPath("$.data.hireDate").value(nullValue()))
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    void updateDuplicateDoesNotChangeEmployee() throws Exception {
        mvc.perform(put("/api/employees/2").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empNo\":\"E001\",\"name\":\"重复\"}"))
                .andExpect(jsonPath("$.code").value(400));
        assertThat(jdbc.queryForObject("SELECT emp_no FROM sys_employee WHERE id=2", String.class))
                .isEqualTo("E002");
    }

    @Test
    void updateRejectsInvalidPhoneAndDepartment() throws Exception {
        for (String field : new String[]{"\"phone\":\"bad\"", "\"deptId\":999"}) {
            mvc.perform(put("/api/employees/2").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"empNo\":\"E002\",\"name\":\"名字\"," + field + "}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(400));
        }
    }

    @Test
    void missingEmployeeReturns404ForReadUpdateAndDelete() throws Exception {
        mvc.perform(get("/api/employees/999")).andExpect(jsonPath("$.code").value(404));
        mvc.perform(put("/api/employees/999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empNo\":\"E999\",\"name\":\"不存在\"}"))
                .andExpect(jsonPath("$.code").value(404));
        mvc.perform(delete("/api/employees/999")).andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void deleteWithSalaryRetiresAndPreservesHistoryAndCanRepeat() throws Exception {
        for (int attempt = 0; attempt < 2; attempt++) {
            mvc.perform(delete("/api/employees/1"))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("已转为离职，保留薪资记录"));
        }
        assertThat(jdbc.queryForObject("SELECT status FROM sys_employee WHERE id=1", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys_salary WHERE emp_id=1", Integer.class)).isEqualTo(1);
    }

    @Test
    void deleteWithoutSalaryRemovesEmployee() throws Exception {
        mvc.perform(delete("/api/employees/2")).andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("员工已删除"));
        mvc.perform(get("/api/employees/2")).andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void departmentDropdownContainsOnlyContractFieldsAndAllowsCors() throws Exception {
        mvc.perform(get("/api/departments").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0]", aMapWithSize(2)))
                .andExpect(jsonPath("$.data[0].deptName").value("技术部"));
    }

    @Test
    void departmentFiltersCombineWithKeywordAndPagination() throws Exception {
        mvc.perform(get("/api/employees/page").param("keyword", " 张 ").param("deptName", " 技术 ")
                        .param("deptId", "1").param("current", "0").param("size", "1000"))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(100))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(1));
        mvc.perform(get("/api/employees/page").param("keyword", "E001").param("deptId", "2"))
                .andExpect(jsonPath("$.data.total").value(0));
        mvc.perform(get("/api/employees/page").param("current", "2").param("size", "2"))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pages").value(2))
                .andExpect(jsonPath("$.data.records", hasSize(1)));
        mvc.perform(get("/api/employees/page").param("size", "-1"))
                .andExpect(jsonPath("$.data.size").value(1));
    }

    @Test
    void unassignedEmployeeAndEmptySearchRemainVisibleAndInjectionIsData() throws Exception {
        mvc.perform(get("/api/employees")).andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[2].deptName").value(nullValue()));
        mvc.perform(get("/api/employees/page").param("keyword", " "))
                .andExpect(jsonPath("$.data.total").value(3));
        mvc.perform(get("/api/employees/page").param("keyword", "' OR 1=1 --"))
                .andExpect(jsonPath("$.data.total").value(0));
        mvc.perform(get("/api/employees/page").param("deptName", "' OR 1=1 --"))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/employees/abc", "/api/employees/0", "/api/employees/-1",
            "/api/employees/page?current=abc", "/api/employees/page?deptId=-1"})
    void invalidPathOrQueryReturnsBusiness400(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(400));
    }

    private ResultActions create(String body) throws Exception {
        return mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
    }
}
