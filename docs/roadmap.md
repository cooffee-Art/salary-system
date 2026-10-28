# 组长进阶路线：从“能跑”到“能交付”

> 任务分下去之后，你最容易犯的错，是继续和组员抢 CRUD 写。
> 组长真正的价值在三个地方：**定规则、兜底横切面、啃最难的业务查询**。

## 1. 先想清楚：你的核心价值是什么

| 组员 | 他们在练什么 | 你应该做什么 |
| --- | --- | --- |
| A | 单表 CRUD、参数校验、Service 分层 | 统一异常、统一返回、接口规范、代码 review |
| B | Vue 页面、组件、联调 | 接口契约、CORS/代理、登录态、部署 |
| C | 表结构、测试用例、验收 | 索引与慢查询、事务与幂等、报表 SQL |

一句话：**他们负责“功能有没有”，你负责“系统稳不稳、能不能讲清楚”。**

## 2. 技术难点优先级

| 优先级 | 方向 | 产出 | 建议投入 |
| --- | --- | --- | --- |
| P0 | 全局异常 + 参数校验统一 | 所有错误都返回 `Result`，前端不用猜 | 半天 |
| P0 | 接口文档（SpringDoc + Knife4j） | 答辩时能打开 `/doc.html` 演示 | 半天 |
| P1 | JWT 登录 + 拦截 + 角色 | 登录页、Token、401 统一处理 | 2 天 |
| P1 | 薪资报表连表查询 | 部门月度薪资汇总、员工薪资历史 | 2 天 |
| P2 | 薪资生成事务 + 幂等 | 批量生成不重复、不半途而废 | 1 天 |
| P2 | 部署与演示 | `docker compose` 或 `java -jar` 一键起 | 1 天 |
| P3 | Redis 缓存 / 审计日志 | 加分项，不做也能答辩 | 有余力再说 |

> 不要碰：微服务、消息队列、Elasticsearch、工作流引擎。四个人、两周，这些只会让你讲不清楚。

## 3. P0：全局异常处理（建议明天就做）

A 用了 `@Valid` 之后，参数错误会抛 `MethodArgumentNotValidException`，默认返回的 JSON 不是你的 `Result` 结构。你不补这个，A 的“参数校验”就不算完成。

### 3.1 业务异常

```java
package com.example.salarysystem.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
```

### 3.2 全局处理器

```java
package com.example.salarysystem.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidation(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("参数不合法");
        return Result.error(400, message);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("唯一约束冲突", e);
        return Result.error(400, "数据已存在，请检查工号或月份是否重复");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnknown(Exception e) {
        log.error("未处理异常", e);
        return Result.error(500, "服务器开小差了，请稍后再试");
    }
}
```

做完后，让 A 把“重复工号”“参数错误”的用例重新跑一遍，确认返回结构统一。

## 4. P1：JWT 登录拦截

这是答辩最容易出彩、也最容易翻车的部分。建议用 **Spring Security 7 + JWT**（Spring Boot 4 对应 Spring Security 7.x），不要自己写拦截器 + 手写 Session。

### 4.1 最小可行方案

1. 加依赖：`spring-boot-starter-security`、`jjwt-api`、`jjwt-impl`、`jjwt-jackson`（0.12.x）。
2. 建表：`sys_user`（id、username、password、role、status）。密码用 `BCryptPasswordEncoder` 存哈希，绝不存明文。
3. 登录接口：`POST /api/auth/login`，校验用户名密码，签发 JWT（2 小时过期），返回 `{ token, username, role }`。
4. 过滤器：继承 `OncePerRequestFilter`，从 `Authorization: Bearer xxx` 取 Token，解析成功后写入 `SecurityContextHolder`。
5. 安全链：`SecurityFilterChain` 设 `stateless`、关 `csrf`、放行 `/api/auth/**`、`/doc.html`、`/v3/api-docs/**`、`OPTIONS`，其余 `authenticated()`。
6. 统一 401/403：实现 `AuthenticationEntryPoint` 和 `AccessDeniedHandler`，返回 `Result`，不要返回 Spring 默认 HTML。

### 4.2 常见坑

- 前端 CORS 预检 `OPTIONS` 被拦截 → 在安全链里 `permitAll()`。
- 登录接口本身被拦截 → 放行 `/api/auth/**`。
- Token 密钥硬编码在代码里 → 放 `application.yaml`，用环境变量覆盖。
- 前端把 Token 放 localStorage → 学生项目可以接受，但要讲清楚 XSS 风险。
- 401 返回 HTML → 前端 axios 拦截器无法统一处理，必须自定义 EntryPoint。

### 4.3 角色怎么设计

四个人、两周，别上完整的 RBAC 五张表。先做最小版：

```text
sys_user(id, username, password, role, status)
role: ADMIN / HR / EMPLOYEE
```

等主流程跑通，再考虑 `sys_role`、`sys_permission`。答辩时能讲清楚“为什么先简单、后续怎么扩展”，比堆表更加分。

## 5. P1：复杂连表查询（最能体现业务价值）

员工 CRUD 谁都会写，**薪资报表**才是这个系统的核心。

### 5.1 部门月度薪资汇总

```sql
SELECT d.id            AS dept_id,
       d.dept_name,
       s.salary_month,
       COUNT(DISTINCT e.id)          AS emp_count,
       SUM(s.base_pay)               AS total_base,
       SUM(s.bonus)                  AS total_bonus,
       SUM(s.deduction)              AS total_deduction,
       SUM(s.actual_pay)             AS total_actual
FROM sys_salary s
JOIN sys_employee e ON e.id = s.emp_id
JOIN sys_department d ON d.id = e.dept_id
WHERE s.salary_month = #{month}
GROUP BY d.id, d.dept_name, s.salary_month
ORDER BY total_actual DESC;
```

对应的 VO：

```java
@Data
public class DeptSalaryReportVO {
    private Integer deptId;
    private String deptName;
    private String salaryMonth;
    private Integer empCount;
    private BigDecimal totalBase;
    private BigDecimal totalBonus;
    private BigDecimal totalDeduction;
    private BigDecimal totalActual;
}
```

### 5.2 员工薪资历史

```sql
SELECT s.id, s.salary_month, s.base_pay, s.bonus, s.deduction, s.actual_pay
FROM sys_salary s
WHERE s.emp_id = #{empId}
ORDER BY s.salary_month DESC
LIMIT #{offset}, #{size};
```

### 5.3 要讲清楚的三个点

1. **为什么用 JOIN 而不是循环查**：避免 N+1。
2. **索引怎么加**：`sys_salary(emp_id, salary_month)`、`sys_salary(salary_month)`、`sys_employee(dept_id)`。
3. **怎么验证**：`EXPLAIN` 看 `type`、`rows`、`Extra`，别只看“能查出来”。

### 5.4 加分项：导出 Excel

用 EasyExcel 把报表导出，答辩时点一下“导出”，比纯表格更有说服力。

## 6. P2：薪资生成的事务与幂等

真正容易出事故的是“批量生成工资”：

```java
@Transactional(rollbackFor = Exception.class)
public void generateMonthlySalary(String month) {
    // 1. 校验月份格式、是否已生成
    // 2. 查出所有在职员工
    // 3. 逐条计算 base/bonus/deduction/actual
    // 4. saveBatch 批量插入
    // 5. 任何一条失败，整批回滚
}
```

要点：

- `actual_pay = base_pay + bonus - deduction`，用 `BigDecimal`，指定 `RoundingMode.HALF_UP`。
- 给 `sys_salary(emp_id, salary_month)` 加唯一索引，重复生成直接报错，避免重复发薪。
- 捕获 `DuplicateKeyException`，返回“该月工资已生成”，实现幂等。
- 不要用 `double` 算钱。

## 7. 两周节奏建议

| 时间 | 你做什么 | 组员做什么 |
| --- | --- | --- |
| 第 1 周前半 | 全局异常、接口文档、CORS 收口 | A CRUD、B 页面、C 评审 |
| 第 1 周后半 | JWT 登录、Security 配置、401 统一 | 三人联调、修 Bug |
| 第 2 周前半 | 薪资报表 SQL、导出、索引优化 | 补测试、补页面、补数据 |
| 第 2 周后半 | 薪资生成事务、部署、演示脚本 | 全量回归、录演示视频 |

## 8. 明天就可以做的三件事

1. **把全局异常处理器写进项目**，让 A 的 `@Valid` 有统一出口。
2. **把 `docs/api.md` 冻结**，拉一个 `dev` 分支并设保护，建 PR 模板。
3. **写出“部门月度薪资汇总”SQL**，在 HeidiSQL 里跑通，再让 C 按这个查询补索引。

## 9. 答辩时你能讲什么

- 为什么用 MyBatis-Plus，而不是 JdbcTemplate / JPA？
- 为什么 Entity、VO、DTO 要分开？
- 分页插件是怎么把 SQL 改写成 `LIMIT` 的？
- CORS 为什么不能只在 Controller 上加？
- JWT 无状态登录的完整链路是什么？401 怎么统一？
- 薪资报表 SQL 用了哪些索引？`EXPLAIN` 结果怎么看？
- 批量生成工资怎么保证不重复、不半途而废？

能把这七个问题讲清楚，这个项目就不只是“增删改查作业”，而是一个能拿得出手的工程作品。
