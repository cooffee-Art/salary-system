# 数据库评审报告（组员 C · 姜锋）

> 评审对象：`docs/init.sql` 与本机 `salary_db` 现库。
> 评审目标：核对主键、外键、唯一约束、金额精度、默认值，评估薪资表防重复发薪约束，并给出索引建议。

## 1. ER 关系

```
sys_department (1) ──────< (N) sys_employee (1) ──────< (N) sys_salary
     id PK                    id PK                         id PK
     dept_name                emp_no  UK                     emp_id FK → sys_employee.id
                              dept_id FK → sys_department.id salary_month
                                                             base_pay / bonus / deduction / actual_pay
```

- 部门 1 : N 员工；员工 1 : N 薪资。
- 员工离职（status=0）保留历史薪资，不物理删除，由员工模块删除接口保证。

## 2. 字段与约束评审

| 表 | 字段 | 类型 | 评审结论 |
| --- | --- | --- | --- |
| sys_department | id | INT AI PK | 主键自增，OK |
| sys_department | dept_name | VARCHAR(50) NOT NULL | 建议后续补唯一键 `uk_dept_name`，避免重名部门（当前未加） |
| sys_employee | id | INT AI PK | OK |
| sys_employee | emp_no | VARCHAR(20) NOT NULL UK | 工号唯一，OK |
| sys_employee | dept_id | INT FK → sys_department.id | 外键生效；允许为空表示未分配部门，OK |
| sys_employee | hire_date | DATE | OK |
| sys_employee | status | TINYINT DEFAULT 1 | 默认在职，OK |
| sys_salary | id | INT AI PK | OK |
| sys_salary | emp_id | INT FK → sys_employee.id | 外键生效，OK |
| sys_salary | salary_month | VARCHAR(7) NOT NULL | 格式 `yyyy-MM`，由后端校验正则 |
| sys_salary | base_pay / bonus / deduction / actual_pay | DECIMAL(10,2) | 金额精度 OK，禁止前端用浮点数传值 |
| sys_salary | create_time | DATETIME DEFAULT CURRENT_TIMESTAMP | 默认当前时间，OK |

## 3. 防重复发薪约束（重点结论）

**结论：应添加唯一键 `uk_emp_month(emp_id, salary_month)`，本次已加入 `init.sql` 建表语句。**

- 业务含义：同一员工同一月份只允许一条薪资记录，避免重复发薪。
- 双保险：
  1. Service 层在新增 / 修改前做「同员工同月」预校验，返回明确业务提示；
  2. 数据库唯一键兜底，拦截并发情况下同时通过预检查的重复写入（`DuplicateKeyException` 由异常处理器转为 400）。
- 已在测试库 `salary-test.sql` 同步该唯一键，并用 `SalaryApiTests#duplicateMonthFailsWithoutInserting` 覆盖。

## 4. 索引建议

| 建议索引 | 表.字段 | 理由 |
| --- | --- | --- |
| `uk_emp_month(emp_id, salary_month)` | sys_salary | 唯一约束，同时加速按员工 + 月份查询（已添加） |
| `idx_salary_month(salary_month)` | sys_salary | 薪资列表按月份筛选 |
| `idx_emp_name(name)` | sys_employee | 员工姓名模糊搜索（数据量大时再加） |
| `uk_dept_name(dept_name)` | sys_department | 防止重名部门（待组长确认后加） |

## 5. 金额计算口径

- 实发工资 `actual_pay = base_pay + bonus - deduction`，后端重算，保留两位小数（四舍五入）。
- 校验：基本工资 / 奖金 / 扣款非负；扣款 ≤ 基本工资 + 奖金（实发不允许为负）。
- 前端传入的 `actualPay` 不参与入库，避免篡改金额。

## 6. 与现库核对

- 本机 `salary_db` 三张表、外键、三条初始薪资数据与 `init.sql` 一致。
- 现库若为旧结构、缺 `uk_emp_month`，在测试环境重置时执行本脚本即可；正式数据请先备份。
