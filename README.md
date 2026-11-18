# 员工薪资管理系统（salary-system）

> 4 人学生团队的练手项目：把课堂上的 Spring Boot / MyBatis-Plus / Vue 串成一个能演示、能讲清楚的小系统。

## Demo Run Guide / 演示运行指南

> Teacher/grader: follow this section to run the project.
> 老师/验收人：按这一节操作即可运行项目。

### Step 1 — Open the Project / 打开项目

1. Open IntelliJ IDEA.
2. `File` → `Open`, select the `salary-system` folder (the folder containing `pom.xml`).
3. Click `OK` → `Open as Project`, then wait for Maven import and indexing.
4. If IDEA reports `程序包 org.springframework... 不存在`, right-click `pom.xml` → `Maven` → `Reload project`.

> 不需要手动把 JDBC 驱动 JAR 放进 `lib/`；Maven 会自动下载依赖。

### Step 2 — Prerequisites / 环境要求

| Software | Required version |
| --- | --- |
| JDK | 21+（JDK 25 works） |
| MySQL | 8.0 |
| Node.js | 18+（only for frontend） |
| IntelliJ IDEA | 2023+ |
| Maven | Use the included `mvnw` wrapper |

MySQL must be running before starting the backend.
启动后端前请确认 MySQL 已启动。

### Step 3 — Set Up the Database / 初始化数据库

1. Open MySQL Workbench / HeidiSQL and connect to local MySQL.
2. Open `docs/init.sql`.
3. Select the `salary_db` database and execute the script.

Or use the command line:

```bash
mysql -uroot -p --default-character-set=utf8mb4 < docs/init.sql
```

After execution, these four tables should exist:

```text
sys_user
sys_department
sys_employee
sys_salary
```

> Warning: `docs/init.sql` drops and recreates the four tables.
>
> 警告：脚本会删表重建，只用于本地练习/测试库，不要直接对正式数据执行。

### Step 4 — Configure the Database Password / 配置数据库密码

`src/main/resources/application.yaml` reads the password from an environment variable:

```yaml
password: "${MYSQL_PASSWORD:你的本机密码}"
```

Recommended: in IDEA, open `Run` → `Edit Configurations` → `Environment variables` and add:

```text
MYSQL_PASSWORD=你的MySQL密码
```

Or edit the `password` value directly. Do not commit a real password to a public repository.

### Step 5 — Start the Backend / 启动后端

1. Open `src/main/java/com/example/salarysystem/SalarySystemApplication.java`.
2. Click the green Run button next to `SalarySystemApplication`.
3. Or run in a terminal:

```powershell
.\mvnw.cmd spring-boot:run
```

Success looks like:

```text
Tomcat started on port 8080
Started SalarySystemApplication
```

Backend URL: `http://localhost:8080`

### Step 6 — Start the Frontend / 启动前端

Open a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open:

```text
http://127.0.0.1:5173
```

> The frontend is a Vite project, not a static HTML page; it must be started with `npm run dev`.

### Step 7 — Login / 登录

Default administrator account:

```text
Username / 用户名: admin
Password / 密码:   admin123
```

> Current frontend status: login page and a placeholder home page are available.
>
> Employee / salary management pages are still under development by teammate B.
>
> 当前前端只有登录页和登录成功占位页；员工/薪资页面仍在开发中。

### Step 8 — Verify the Backend API / 验证后端接口

The login endpoint is public; all other `/api/**` endpoints require a Bearer Token.

PowerShell example:

```powershell
$login = Invoke-RestMethod "http://localhost:8080/api/auth/login" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"admin123"}'

$token = $login.data.token

Invoke-RestMethod "http://localhost:8080/api/employees" `
  -Headers @{ Authorization = "Bearer $token" } |
  ConvertTo-Json -Depth 5
```

Opening `http://localhost:8080/api/employees` directly in a browser returns 401 because the browser does not send the Authorization header. This is expected.
浏览器直接打开业务接口会返回 401，这是正常的，需要先登录拿 Token。

### Step 9 — Stop / 停止

- Frontend: press `Ctrl + C` in the `npm run dev` terminal.
- Backend: click the red Stop button in IDEA.

### Common Issues / 常见问题

| Problem | Solution |
| --- | --- |
| IDEA 报 `程序包 org.springframework.security.config 不存在` | Right-click `pom.xml` → `Maven` → `Reload project`; then `Build` → `Rebuild Project` |
| `Access denied for user 'root'` | Check `MYSQL_PASSWORD` / `application.yaml` password |
| `Unknown database 'salary_db'` | Run `docs/init.sql` first |
| `Port 8080 already in use` | Stop the old backend process or change `server.port` |
| `Port 5173 already in use` | Vite will choose 5174; use the URL printed in the terminal |
| 401 on API request | Log in first and add `Authorization: Bearer <token>` |
| Frontend login always succeeds | The frontend still uses Mock; B needs to disable Mock and enable the `/api` proxy |

## 技术栈

| 层次 | 技术 | 版本 |
| --- | --- | --- |
| 后端 | Spring Boot | 4.1.2 |
| 后端 | MyBatis-Plus | 3.5.18（boot4 starter） |
| 后端 | Spring Security + JWT | Security 7 + jjwt 0.12.6 |
| 后端 | MySQL | 8.0 |
| 后端 | JDK | 21（本机用 25 也能编译运行） |
| 前端 | Vue 3 + Vite | 登录页已完成 |
| 前端 | Element Plus + Axios | 已接入 |
| 工具 | IDEA / HeidiSQL / Git / GitHub / Gitee | — |

> 版本注意：Spring Boot 4 的 Web starter 叫 `spring-boot-starter-webmvc`；MyBatis-Plus 3.5.18 的 `IService` / `ServiceImpl` 包名是 `com.baomidou.mybatisplus.spring.service`，旧教程里的 `...extension.service` 会编译不过。

## 已实现

- 员工列表（连表带部门名称）
- 员工分页 + 姓名/工号关键字搜索
- 员工详情
- 员工新增、完整修改、删除/离职（保留薪资历史）
- 员工参数校验、工号唯一检查、部门 ID / 名称组合筛选
- 部门下拉查询（id、deptName）
- JWT 登录、无状态鉴权、统一 401/403 返回
- 薪资分页查询（员工 + 部门 + 薪资三表连查）
- 薪资新增/修改/删除/详情，实发工资由后端重算
- 薪资金额校验：非负、扣款不能大于收入、同员工同月唯一
- 部门月度薪资汇总报表
- 月度工资批量生成（事务 + 唯一约束幂等）
- 统一返回结构 `Result<T>` / `PageResult<T>` 与全局异常处理
- MyBatis-Plus 分页插件、驼峰映射、SQL 控制台日志
- 本地联调 CORS
- 数据库初始化脚本、接口契约、团队任务书

## 目录结构

```text
salary-system/
├── src/main/java/com/example/salarysystem/
│   ├── SalarySystemApplication.java
│   ├── common/                 # Result、PageResult、全局异常
│   ├── config/                 # MyBatis-Plus、Security 配置
│   ├── controller/             # 员工、部门、登录、薪资接口
│   ├── dto/                    # 员工保存、登录参数
│   ├── entity/                 # SysEmployee、SysDepartment、SysSalary、SysUser
│   ├── mapper/                 # 各模块数据库访问
│   ├── security/               # JWT 签发、解析、过滤器
│   ├── service/                # 接口 + impl
│   └── vo/                     # 列表/报表/登录返回对象
├── src/main/resources/
│   └── application.yaml
├── frontend/                   # Vue 3 + Vite 前端
├── docs/
│   ├── init.sql                # 建库、建表、测试数据
│   ├── api.md                  # 前后端接口契约
│   ├── db-review.md            # 数据库评审
│   ├── backlog-b-c.md          # B/C 工作清单
│   ├── team-tasks.md           # 三名组员的任务书
│   ├── test/                   # 测试用例与 Bug 清单
│   └── roadmap.md              # 组长进阶路线
├── pom.xml
└── README.md
```

## 开发环境启动（详细）

### 1. 初始化数据库

> `docs/init.sql` 会先删除四张表再重建，只在本地测试库执行。

```bash
mysql -uroot -p --default-character-set=utf8mb4 < docs/init.sql
```

或者用 HeidiSQL 打开 `docs/init.sql`，确认当前库是 `salary_db` 后执行。

### 2. 配置数据库密码

`application.yaml` 默认读取环境变量 `MYSQL_PASSWORD`，没有就用本地默认值。推荐在 IDEA 的 Run Configuration 里加环境变量：

```text
MYSQL_PASSWORD=你的本机密码
```

也可以直接改 `application.yaml` 的 `password`，但不要把真实密码提交到公开仓库。

### 3. 启动后端

Windows：

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux：

```bash
./mvnw spring-boot:run
```

启动成功后，先登录拿 Token（默认账号 `admin / admin123`，仅本地练习用）：

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

然后把返回的 `data.token` 放到请求头：

```text
Authorization: Bearer <token>
```

再访问：

```text
http://localhost:8080/api/employees
http://localhost:8080/api/employees/page?current=1&size=10&keyword=张
http://localhost:8080/api/salaries/page?month=2024-09
http://localhost:8080/api/salaries/report/department?month=2024-09
```

> 除 `/api/auth/login` 外，所有 `/api/**` 接口都需要 Token。

### 4. 启动前端（组员 B 完成后）

```bash
cd frontend
npm install
npm run dev
```

默认地址 `http://localhost:5173`。

> 当前前端登录仍走 Mock；接真实后端时，关闭 `vite-plugin-mock`、启用 `/api` proxy，并调用 `POST /api/auth/login` 获取真实 Token。后端已提供该接口。

## 接口

完整字段、请求/响应示例、错误码见 [docs/api.md](docs/api.md)。

| 方法 | 路径 | 说明 | 状态 |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | 登录，返回 JWT | 已完成 |
| GET | `/api/auth/me` | 当前登录人 | 已完成 |
| GET | `/api/employees` | 全部员工（带部门名） | 已完成 |
| GET | `/api/employees/page` | 分页 + 关键字搜索 | 已完成 |
| GET | `/api/employees/{id}` | 员工详情 | 已完成 |
| POST | `/api/employees` | 新增员工 | 已完成 |
| PUT | `/api/employees/{id}` | 完整修改员工 | 已完成 |
| DELETE | `/api/employees/{id}` | 删除/离职 | 已完成 |
| GET | `/api/departments` | 部门下拉 | 已完成 |
| GET | `/api/salaries` | 薪资列表（月份/员工/关键字） | 已完成 |
| GET | `/api/salaries/page` | 薪资分页（三表连查） | 已完成 |
| GET | `/api/salaries/{id}` | 薪资详情 | 已完成 |
| POST | `/api/salaries` | 新增薪资，后端重算实发 | 已完成 |
| PUT | `/api/salaries/{id}` | 修改薪资 | 已完成 |
| DELETE | `/api/salaries/{id}` | 删除薪资 | 已完成 |
| GET | `/api/salaries/report/department` | 部门月度汇总 | 已完成 |
| POST | `/api/salaries/generate` | 批量生成月度工资 | 已完成 |

## 分支与提交规范

| 分支 | 用途 | 谁能合并 |
| --- | --- | --- |
| `main` | 可演示版本，答辩/展示用 | 组长 |
| `dev` | 集成分支，日常联调 | 组长 |
| `feature/xxx` | 个人功能分支 | 本人推送，PR 到 dev |
| `fix/xxx` | 修 Bug | 本人推送，PR 到 dev |

提交信息格式：

```text
feat(employee): 新增员工接口
fix(salary): 修复月份查询越界
docs(api): 补充部门下拉字段
refactor(common): 抽取 PageResult
```

## 团队约定

1. 每天 21:00 前把代码推到自己的 `feature` 分支，21:30 群里 15 分钟站会。
2. PR 必须写清楚：改了什么、怎么自测、截图或请求示例。
3. 接口字段以 `docs/api.md` 为准，要改先群里说。
4. 不提交 `target/`、`.idea/`、`node_modules/`、真实数据库密码。
5. 完成的定义：本地能跑 + 自测通过 + 文档更新 + PR 已提。

## 常见问题（详细排查）

### IDEA 报 `程序包 org.springframework.security.config 不存在`

命令行 `mvn clean test` 能通过时，通常是 IDEA 的 Maven 模型没有刷新：

1. 右键 `pom.xml` → `Maven` → `Reload project`；
2. 或打开 Maven 工具窗，点刷新按钮；
3. 仍然报错：`File` → `Invalidate Caches...` → `Invalidate and Restart`；
4. 检查 `File` → `Project Structure` → `Modules` → `salary-system` → `Dependencies`，应能看到 `org.springframework.security:spring-security-config:7.1.2`。

项目已在 `pom.xml` 中显式声明 `spring-security-config`、`spring-security-web`、`spring-security-crypto`，避免 IDE 只索引直接依赖时找不到包。

## 文档

- [接口契约](docs/api.md)
- [数据库初始化脚本](docs/init.sql)
- [数据库评审报告](docs/db-review.md)
- [测试用例](docs/test/cases.md)
- [Bug 清单](docs/test/bugs.md)
- [组员任务书](docs/team-tasks.md)
- [B/C 当前迭代工作清单](docs/backlog-b-c.md)
- [员工模块开发、测试与交接说明](docs/employee-development.md)
- [Postman 员工管理集合](docs/postman/employee-management.postman_collection.json)
- [组长进阶路线](docs/roadmap.md)
