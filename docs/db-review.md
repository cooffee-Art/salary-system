# 数据库评审报告

> 评审对象：`docs/init.sql` 与本机 `salary_db` 现库。
> 评审目标：核对主键、外键、唯一约束、金额精度、默认值，评估薪资防重复发薪约束，并给出索引建议。

## 1. ER 关系

```text
sys_department (1) ──────< (N) sys_employee (1) ──────< (N) sys_salary
     id PK                    id PK                         id PK
     dept_name                emp_no  UK                     emp_id FK → sys_employee.id
                              dept_id FK → sys_department.id salary_month
                                                             base_pay / bonus / deduction / actual_pay

sys_user（独立表，不参与上述业务外键）
     id PK
     username UK
     password（BCrypt）
     role / status
```

- 部门 1:N 员工；员工 1:N 薪资。
- 员工离职（status=0）保留历史薪资，不物理删除，由员工模块删除接口保证。
- `sys_user` 只用于登录鉴权，与员工/薪资没有外键关系。

## 2. 字段与约束评审

| 表 | 字段 | 类型 | 评审结论 |
| --- | --- | --- | --- |
| sys_user | id | INT AI PK | OK |
| sys_user | username | VARCHAR(50) NOT NULL UK | 登录名唯一，OK |
| sys_user | password | VARCHAR(100) NOT NULL | 存 BCrypt 哈希，不存明文，OK |
| sys_user | role | VARCHAR(20) | ADMIN/HR/EMPLOYEE，当前仅写入 JWT，未做接口级权限 |
| sys_department | id | INT AI PK | OK |
| sys_department | dept_name | VARCHAR(50) NOT NULL | 建议后续补唯一键 `uk_dept_name`，避免重名部门（当前未加） |
| sys_employee | id | INT AI PK | OK |
| sys_employee | emp_no | VARCHAR(20) NOT NULL UK | 工号唯一，OK |
| sys_employee | dept_id | INT FK → sys_department.id | 外键生效；允许为空表示未分配部门，OK |
| sys_employee | hire_date | DATE | OK |
| sys_employee | status | TINYINT DEFAULT 1 | 默认在职，OK |
| sys_salary | id | INT AI PK | OK |
| sys_salary | emp_id | INT FK → sys_employee.id | 外键生效，OK |
| sys_salary | salary_month | VARCHAR(7) NOT NULL | 格式 `yyyy-MM`，由后端校验 |
| sys_salary | base_pay / bonus / deduction / actual_pay | DECIMAL(10,2) | 金额精度 OK，禁止前端用浮点数传值 |
| sys_salary | create_time | DATETIME DEFAULT CURRENT_TIMESTAMP | 默认当前时间，OK |

## 3. 防重复发薪约束（重点结论）

**结论：已添加唯一键 `uk_emp_month(emp_id, salary_month)`。**

- 业务含义：同一员工同一月份只允许一条薪资记录，避免重复发薪。
- 双保险：
  1. Service 层新增/修改前做「同员工同月」预校验，返回明确业务提示；
  2. 数据库唯一键兜底，拦截并发情况下同时通过预检查的重复写入（`DuplicateKeyException` 统一转为 400）。
- 自动化测试：`SalaryCrudApiTests#duplicateMonthFailsWithoutInserting`。

## 4. 索引建议

| 索引 | 表.字段 | 状态 |
| --- | --- | --- |
| `uk_emp_month(emp_id, salary_month)` | sys_salary | 已添加，唯一约束 + 加速员工/月份查询 |
| `idx_salary_month(salary_month)` | sys_salary | 已添加，报表按月份筛选 |
| `idx_emp_name(name)` | sys_employee | 数据量大时再加 |
| `uk_dept_name(dept_name)` | sys_department | 建议后续加，防止重名部门 |

## 5. 金额计算口径

- 实发工资 `actual_pay = base_pay + bonus - deduction`，后端重算，保留两位小数（四舍五入）。
- 校验：基本工资/奖金/扣款非负；扣款 ≤ 基本工资 + 奖金（实发不允许为负）。
- 前端传入的 `actualPay` 不参与入库，避免篡改金额。

## 6. 与现库核对

- 本机 `salary_db` 已包含四张表、外键、三条 2024-09 薪资数据和 `admin` 登录用户。
- `uk_emp_month`、`idx_salary_month` 已应用到本地库。
- 现库若为旧结构、缺 `uk_emp_month` 或 `sys_user`，在测试环境执行 `docs/init.sql` 即可；正式数据请先备份。
- 已知限制：角色目前只写入 JWT，尚未做接口级权限；部门名称唯一键待评估。
