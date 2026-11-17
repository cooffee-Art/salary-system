# 员工模块开发与交接说明

## 本次完成内容

分支：`feature/employee-crud`。在原有 MyBatis-Plus、列表、详情和分页基础上完成：

| 功能 | 接口 / 行为 |
| --- | --- |
| 新增员工 | `POST /api/employees`，返回含自增 ID 的员工 |
| 完整修改员工 | `PUT /api/employees/{id}`，支持清空可选字段 |
| 删除 / 离职 | `DELETE /api/employees/{id}`，有薪资则离职，无薪资才物理删除 |
| 部门下拉 | `GET /api/departments`，只返回 id、deptName |
| 条件分页 | 保留 keyword，新增 deptId 精确筛选、deptName 模糊筛选；条件 AND 组合 |
| 参数与错误 | DTO + Jakarta Validation；工号唯一、部门存在、日期及手机号校验；统一 Result |

员工 ID 不存在返回业务码 404；参数和工号冲突返回业务码 400。
沿用现有契约：HTTP 200，前端读取 body.code 判断业务成功与否。

## 代码位置与职责

- `controller/EmployeeController.java`：员工 HTTP 接口。
- `dto/EmployeeSaveDTO.java`：新增、完整修改请求；字符串去首尾空白与参数校验。
- `service/impl/SysEmployeeServiceImpl.java`：唯一性校验、部门检查、事务、修改和删除规则。
- `mapper/SysEmployeeMapper.java`：组合查询、员工行锁和薪资记录数量查询。
- `entity/SysDepartment.java`、`mapper/SysDepartmentMapper.java`、部门 Service / Controller：部门下拉。
- `common/BusinessException.java`、`controller/EmployeeExceptionHandler.java`：模块业务错误与校验错误返回。
- `src/test/.../EmployeeApiTests.java`：真实 Controller → Service → Mapper → 数据库的接口集成测试。

异常处理器仅作用于员工和部门 Controller，后续可由组长整合到全局异常处理。
没有修改数据库表结构、登录认证、薪资计算及部门增删改业务。

## 给前端的对接要点

1. 字段、示例见 `docs/api.md`；旧查询 URL 仍然可用。
2. PUT 是完整替换：empNo、name 必传；其他字段省略/null 会清空，status 默认 1。
3. 新增和修改不传 id、deptName；入职日期使用 `yyyy-MM-dd`。
4. 删除成功后刷新列表：已有薪资的员工仍会显示，状态变为离职；可以用响应 data 提示结果。
5. 部门下拉只提供读取接口，部门维护仍由组长负责。

## 已执行的验证

验证日期：2026-11-16。

| 检查 | 结果 |
| --- | --- |
| JDK 21 + 项目原有 Spring Boot / MyBatis-Plus 版本编译 | 通过 |
| Maven 自动测试 | 30 项通过，0 失败，0 错误 |
| Maven package 与 Spring Boot 可执行 JAR 打包 | 通过，target/salary-system-0.0.1-SNAPSHOT.jar |
| 独立端口启动 Spring Boot + Tomcat | 通过，测试端口 18080 |
| Newman 执行随附 Postman 集合，真实 HTTP 请求 | 14 请求、37 断言通过，0 失败 |
| 不存在的日期 2024-02-30 | HTTP 请求返回业务码 400 |
| MySQL 8.0.44 实库自动测试 | 独立实例 127.0.0.1:3307，salary_db_employee_test 中 30 项全部通过 |
| MySQL 实库 HTTP / Newman 联调 | 本机 8080 后端连接 salary_db，14 请求、37 断言全部通过 |

首次自动测试和临时 18080 HTTP 验证使用 H2 的 MySQL 兼容模式。
随后按用户要求，沿用项目默认密码建立独立 MySQL 实例，在专用测试库复跑全套测试，
并使用打包后的后端连接 MySQL 开发库完成真实 HTTP 联调。
原有 3306 实例的默认凭据登录失败问题，通过独立 3307 实例解决；没有修改原有实例的账号或数据。
独立实例的 salary_db 使用仓库 docs/init.sql 建表和导入初始数据。
随附微信 SQL 是仅数据导出，不含建表语句；本次使用完整的仓库初始化脚本。

主要用例覆盖：

- 新增、默认状态、空白规范化、工号重复。
- 必填、最大长度、性别、手机号、未来入职日期、非法日期、非法状态和无效部门。
- 修改保留原工号、与他人工号冲突、清空可选字段、修改失败不写入。
- 不存在员工的查询 / 修改 / 删除，非法 ID / 数字参数。
- 有薪资离职并保留历史、重复离职、无薪资物理删除。
- 部门下拉字段与 CORS，员工未分配部门时仍可查询。
- 部门与姓名/工号组合查询、分页边界、空白搜索及 SQL 注入字符串。

## 如何复现自动测试

项目要求 JDK 21 或兼容版本，Maven 设置有效时，在项目根目录执行：

```powershell
mvn test
```

测试自动启用 test profile，默认连接内存数据库，不需要本地 MySQL。
H2 依赖只在 test 范围，不会改变正式运行时的 MySQL 配置。

MySQL 实库回归时，先单独创建 salary_db_employee_test（测试会重建其中的三张表），
再用环境变量覆盖测试 datasource。示例 URL 仅指向专用测试库，不能替换为已有业务数据库：

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://127.0.0.1:3307/salary_db_employee_test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true'
$env:SPRING_DATASOURCE_DRIVER_CLASS_NAME = 'com.mysql.cj.jdbc.Driver'
$env:SPRING_DATASOURCE_USERNAME = 'root'
# 使用独立实例的密码；本机此实例沿用项目默认密码。
$projectConfig = Get-Content src/main/resources/application.yaml -Raw
$env:SPRING_DATASOURCE_PASSWORD = [regex]::Match($projectConfig, '\$\{MYSQL_PASSWORD:([^}]+)\}').Groups[1].Value
mvn test
# 执行后清除这些临时环境变量，恢复默认 H2 测试配置。
Remove-Item Env:SPRING_DATASOURCE_URL, Env:SPRING_DATASOURCE_DRIVER_CLASS_NAME, Env:SPRING_DATASOURCE_USERNAME, Env:SPRING_DATASOURCE_PASSWORD
```

本机默认 Java 为 11，且安装的 Maven 全局 settings.xml 语法错误。
验证时使用以下临时配置绕过，没有修改系统安装或项目依赖版本：

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$settingsFile = Join-Path $env:TEMP 'salary-maven-settings.xml'
Set-Content -LiteralPath $settingsFile -Encoding utf8 `
  -Value '<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0" />'
mvn -gs $settingsFile -s $settingsFile test
```

## 使用 Postman / Newman 联调

1. 在 Postman 导入 `docs/postman/employee-management.postman_collection.json`。
2. 设置集合变量 baseUrl，例如 `http://localhost:8080`。
3. 测试库至少有一个部门；按 01–14 的顺序运行集合。
4. 集合自动创建唯一工号的员工，保存其 ID，完成查询、修改、删除，并验证错误场景。
5. 集合只修改自己创建的员工；有薪资转离职由自动测试覆盖，手动联调须使用专用测试员工。

也可使用 Newman：

```powershell
npx.cmd --yes newman run docs/postman/employee-management.postman_collection.json `
  --env-var 'baseUrl=http://localhost:8080'
```

若要复现本次不依赖 MySQL 的 HTTP 验证，先运行 `mvn test`，再启动：

```powershell
$testClasses = (Resolve-Path target/test-classes).Path
mvn -gs $settingsFile -s $settingsFile spring-boot:run `
  '-Dspring-boot.run.useTestClasspath=true' `
  "-Dspring-boot.run.additional-classpath-elements=$testClasses" `
  '-Dspring-boot.run.profiles=test' `
  '-Dspring-boot.run.arguments=--server.port=18080 --spring.sql.init.mode=always --spring.sql.init.schema-locations=classpath:employee-test.sql'
```

在另一终端运行 Newman，将 baseUrl 改为 `http://localhost:18080`。
此命令只用于临时内存测试。普通 MySQL 启动沿用 README，需设置正确的 MYSQL_PASSWORD；
不要把真实密码写入提交内容，也不要在已有业务数据的库运行会删表的 init.sql。

## 本机已准备的运行环境

- MySQL：127.0.0.1:3307，用户 root，密码沿用项目默认值。
- salary_db 是开发库，部门 / 员工 / 薪资初始记录各 3 条；专用回归库为 salary_db_employee_test。
- 根目录 application-local.yaml 覆盖本机数据库地址，IDEA Active profiles 填 local 即可连接。
- 本机已启动打包后端，地址为 http://localhost:8080/api/employees。
- .local/README.md 包含本机启动方式；IDEA 开发前可运行 .local/stop-backend.ps1 关闭占用 8080 的打包后端。
- .local/start-local.ps1 可重新启动独立数据库和打包后端；未设置开机自启。
- application-local.yaml、.local/ 数据、日志及脚本均已被 Git 忽略，不随代码提交。

## 提交与组长对接

PR 标题：`feat(employee): 完成员工 CRUD、部门下拉与条件筛选`。

PR 可引用本文件的功能及验证结果，目标分支为 dev。需同步给组长 / 前端的变化：
新增查询参数、PUT 完整替换规则、删除后的状态与提示，以及模块异常处理器的合并方式。
代码、接口文档、测试及 Postman 集合通过 feature/employee-crud 分支提交，PR 目标为 dev，由组长审核合并。
