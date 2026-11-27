# 控制台版测试报告

> 负责人：Lucas（姜锋）· 分支：`feature/console-refactor-db-auth`
> 用例清单见 `docs/test/console-test-cases.md`，原始输出见 `docs/test/evidence/`

## 1. 测试环境

| 项目 | 版本 / 说明 |
| --- | --- |
| 操作系统 | Windows 11（中文，控制台默认代码页 936） |
| JDK | OpenJDK 25（`javac` / `java`） |
| 数据库 | MySQL 8.0.12（127.0.0.1:3306） |
| 驱动 | mysql-connector-j-8.4.0（`lib/`，jar 不入库） |
| 测试库 | `salary_db_console_test`（脚本中库名替换为它，避免覆盖已有 Web 版数据） |
| 构建 | `javac -encoding UTF-8 -d out src/**/*.java`，无 Maven/Gradle |

## 2. 结论摘要

| 类别 | 用例数 | 通过 | 失败 | 备注 |
| --- | --- | --- | --- | --- |
| 登录与账号 AU | 13 | 13 | 0 | 含注入、停用账号 |
| 角色权限 PM | 8 | 8 | 0 | 权限矩阵白盒 |
| 双语 I18N | 7 | 7 | 0 | 中英各跑完整流程 |
| 数据库初始化 DB | 12 | 12 | 0 | 空库建库 + 重复执行 + 金额一致性 |
| 数据校验 V | 11 | — | — | 待 Ming 的 `Validator` 合并后执行 |
| 白盒自检合计 | 65 项断言 | 65 | 0 | `java ... SelfCheck` 退出码 0 |

- 编译：`javac` 退出码 0，无警告级错误。
- 启动：有 MySQL 时走 JDBC 登录；MySQL 不可用时自动退回内存演示账号，程序不崩。
- 源码无框架依赖：`grep -ri "spring|vue" src/ resources/` 无匹配。

## 3. 关键实测记录

### 3.1 数据库初始化

```text
表名    记录数
部门    6
员工    13
用户    4
薪资    39

检查项              异常数
实发金额异常行数    0
扣款超额行数        0

部门    在职人数  九月实发合计
技术部  3        28050.00
财务部  2        16350.00
人事部  2        14700.00
市场部  2        16070.00
行政部  2        14250.00
运维部  1         8320.00
```

约束核对（`information_schema`）：

- 唯一键：`sys_department.uk_dept_name`、`sys_employee.uk_emp_no / uk_email / uk_phone`、`sys_salary.uk_emp_month`、`sys_user.uk_username`
- 外键：`sys_employee.fk_emp_dept`、`sys_salary.fk_salary_emp`、`sys_user.fk_user_emp`
- `sys_user.password_hash`：`pbkdf2$sha256$120000$…`，长度 90，无明文
- 重复执行脚本第二次仍成功，计数不变（DROP + 重建幂等）

### 3.2 中文流程（黑盒）

语言选中文 → `admin/Admin@123` → 选员工管理 → 退出登录 → `employee/User@123` → 我的信息 → 我的薪资 → 退出。

实际输出节选：

```text
已连接 MySQL，使用数据库账号登录。
请选择：语言已切换为中文。
用户名（输入 0 退出）：密码：登录成功，欢迎 admin（管理员）。
请选择：员工管理 模块正在开发中。
请选择：已退出登录。
用户名（输入 0 退出）：密码：登录成功，欢迎 employee（普通员工）。
请选择：我的信息 模块正在开发中。
```

全中文无乱码，员工菜单 4 项、管理员菜单 5 项，与 `docs/test/console-test-cases.md` PM-01/PM-02 一致。

### 3.3 英文流程（黑盒）

```text
Connected to MySQL. Signing in with database accounts.
Language switched to English.
Username (enter 0 to exit):Password:Login successful. Welcome, admin (Administrator).
Select an option:Employee Management is under development.
Select an option:You have been logged out.
Username (enter 0 to exit):Password:Login successful. Welcome, employee (Employee).
Select an option:My Profile is under development.
```

### 3.4 错误密码流程（黑盒）

`admin` + `wrongpass` 后：

```text
用户名或密码错误，请重试。
```

停在登录页，再次输入正确密码可进入主菜单，随后 `0` 正常退出。符合 AU-03。

### 3.5 数据库不可用时（黑盒）

把 `SALARY_DB_URL` 指向 `localhost:3399`：

```text
未连接到 MySQL，已切换为内存演示账号登录。
连接失败原因：Communications link failure
...
Login successful. Welcome, admin (Administrator).
```

程序可启动、可登录、可退出，符合 DB-09。

### 3.6 白盒自检（65 项断言，全部 PASS）

```text
[1] 密码哈希 PBKDF2        8 项
[2] 角色权限矩阵            9 项
[3] 双语语言包完整性       34 项
[4] JDBC 登录（sys_user）  11 项
[5] SQL 注入防护            2 项
[6] 用户名唯一键            3 项
结果：通过 65 项，失败 0 项
```

复现命令：

```bash
javac -encoding UTF-8 -cp "out" -d test-out test/SelfCheck.java
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "out;test-out;resources;lib/*" SelfCheck
```

## 4. 发现记录

| 编号 | 现象 | 结论 / 处理 |
| --- | --- | --- |
| ISS-01 | 中文 Windows 下重定向输出中文变乱码 | Java 18+ 起 `stdout.encoding` 跟随控制台代码页（936），仅设 `-Dfile.encoding=UTF-8` 不够。运行前 `chcp 65001`，或加 `-Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8`，输出即正常。已写入报告供全班参考 |
| ISS-02 | README 规定的分支名 `feature/console-refactor/db-auth` 无法创建 | 父分支 `feature/console-refactor` 已占用同名路径，Git 报 refs 冲突（远程同样拒绝）。参照 Leo 的 `feature/console-refactor-salary`，实际用 `feature/console-refactor-db-auth`，PR 说明中已注明 |
| ISS-03 | `validation/Validator.java` 尚未合并 | V-01~V-11 无法端到端执行，用例已登记，等 Ming 的 PR 进来后补跑 |
| ISS-04 | 业务菜单仍是 `notImplemented` 占位 | 属正常阶段状态：权限拦截已先于业务落地，菜单点进去会先过 `PermissionGuard` 再提示“模块正在开发中” |

## 5. 未覆盖 / 风险

- 真实库 `salary_db` 未执行过 `init.sql`（会 DROP 旧 Web 版数据），所有验证在 `salary_db_console_test` 完成。正式切换前需确认旧数据可弃或先备份。
- 并发登录、连接池未涉及：控制台单用户场景，每次操作新开连接即可。
- 性能未测：课程项目规模，无此需求。
