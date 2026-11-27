# 数据库设计说明（Console Edition）

> 负责人：Lucas（姜锋）· 分支：`feature/console-refactor-db-auth`
> 脚本：`sql/init.sql`　｜　访问方式：原生 JDBC（`java.sql`），无 ORM、无框架

## 1. 设计目标

- 一套脚本从零建库到位：建库、建表、建约束、灌测试数据、自检计数。
- 登录与业务共用同一个 `salary_db`，四张表职责不重叠。
- 金额用 `DECIMAL(10,2)`，密码用哈希，全部约束落到数据库层，不只靠 Java 校验。

## 2. ER 关系

```text
sys_department (1) ──────< (N) sys_employee (1) ──────< (N) sys_salary
      id PK                     id PK                     id PK
      dept_name UK              emp_no UK                  emp_id FK → sys_employee.id
                                email UK                   salary_month CHAR(7)
                                phone UK                   base_pay / bonus / deduction / actual_pay
                                dept_id FK → department

sys_user（登录账号表）
      id PK
      username UK
      password_hash（PBKDF2 哈希，不存明文）
      role ENUM('ADMIN','EMPLOYEE')
      status TINYINT
      emp_id FK → sys_employee.id（可空；普通员工账号绑定到具体员工）
```

说明：

- 部门 1:N 员工，员工 1:N 薪资记录。
- `sys_user.emp_id` 把登录账号与员工绑定，普通员工登录后只能查自己的信息与薪资（对应“我的信息 / 我的薪资”）。
- 管理员账号 `emp_id` 为空：管理员管全公司，不归属某个员工。

## 3. 表与约束

| 表 | 字段要点 | 约束 |
| --- | --- | --- |
| `sys_department` | `dept_name` | PK `id`；UK `uk_dept_name`（部门名唯一，避免报表出现同名部门） |
| `sys_employee` | `emp_no`、`name`、`gender`、`email`、`phone`、`dept_id`、`hire_date`、`status` | PK `id`；UK `uk_emp_no` / `uk_email` / `uk_phone`；FK `fk_emp_dept`；索引 `idx_emp_name` |
| `sys_user` | `username`、`password_hash`、`role`、`status`、`emp_id` | PK `id`；UK `uk_username`；FK `fk_user_emp` |
| `sys_salary` | `emp_id`、`salary_month`、`base_pay`、`bonus`、`deduction`、`actual_pay` | PK `id`；UK `uk_emp_month(emp_id, salary_month)`；FK `fk_salary_emp`；索引 `idx_salary_month` |

关键设计点：

1. **防重复发薪**：`uk_emp_month` 保证同一员工同一月只有一条薪资记录，业务层的“已存在”预检查只是提示，最终由数据库兜底。
2. **三唯一键对应校验用例**：工号、邮箱、手机号各自唯一，`V-08`（重复数据）在库层面就会失败。
3. **金额精度**：`DECIMAL(10,2)`，不用 `FLOAT/DOUBLE`，避免发薪对账出现浮点误差。
4. **角色类型**：`ENUM('ADMIN','EMPLOYEE')`，写错角色直接报错，不会存进非法值。
5. **密码**：`VARCHAR(200)` 存 `pbkdf2$sha256$迭代$盐$哈希`（本机默认 120000 次迭代，90 字符），不存明文。

## 4. 默认账号与测试数据

| 角色 | 用户名 | 密码 | 绑定员工 | 用途 |
| --- | --- | --- | --- | --- |
| 管理员 | `admin` | `Admin@123` | — | 演示登录、员工/薪资/报表 |
| 普通员工 | `employee` | `User@123` | 2 李娜（财务部） | 验证角色菜单与越权拦截 |
| 普通员工 | `zhangwei` | `Employee@1` | 1 张伟（技术部） | 验证“我的信息 / 我的薪资”按人取数 |
| 普通员工 | `machao` | `Employee@1` | 12 马超（运维部，已离职） | 验证离职员工数据的查询口径 |

测试数据规模：**6 个部门、13 名员工（12 在职 + 1 离职）、4 个登录账号、39 条薪资记录**（2024-07 / 08 / 09 连续三个月，13 人 × 3 个月）。

- 部门：技术部、财务部、人事部、市场部、行政部、运维部，每个部门都有在职员工
- 员工：工号 `E001`~`E013`，邮箱、手机号互不重复，覆盖 6 个部门
- 薪资：每月每条记录满足 `实发 = 基本工资 + 奖金 - 扣款`，且扣款不超过“基本工资 + 奖金”
- 脚本末尾会输出各部门在职人数与当月实发合计，可直接对照报表口径

## 5. 初始化步骤

```bash
# 1. 设置数据库密码（不要写进代码或提交到仓库）
# Windows PowerShell
$env:SALARY_DB_PASSWORD="你的MySQL密码"
# macOS / Linux
export SALARY_DB_PASSWORD="你的MySQL密码"

# 2. 执行脚本
mysql -uroot -p --default-character-set=utf8mb4 < sql/init.sql

# 也可用 HeidiSQL / MySQL Workbench 打开 sql/init.sql 执行
```

脚本末尾会输出四张表的记录数，正常应为：部门 6、员工 13、用户 4、薪资 39；随后两条校验应输出「实发金额异常行数 0」「扣款超额行数 0」。

## 6. 连接配置

`src/com/salary/console/config/DatabaseConfig.java` 读取环境变量，优先级：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SALARY_DB_URL` | `jdbc:mysql://localhost:3306/salary_db?...` | 端口或库名变更时覆盖 |
| `SALARY_DB_USER` | `root` | 数据库用户 |
| `SALARY_DB_PASSWORD` | `your_mysql_password`（占位） | **必须在本机设置**，真实密码不提交 |

> 为什么不用“把密码写死在常量里”：README 第 6 节要求不要提交真实密码。改成环境变量后，仓库里永远只有占位值，本机开发者各自设置即可。

## 7. 已知限制

- `sys_user` 没有“最后登录时间”字段，登录日志暂不落库。
- 薪资记录不支持软删除，删除即物理删除（控制台版按课程要求简化）。
- 未做分表/归档，数据量按课程项目规模设计。
