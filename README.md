# 员工薪资管理系统（salary-system）

> 4 人学生团队的练手项目：把课堂上的 Spring Boot / MyBatis-Plus / Vue 串成一个能演示、能讲清楚的小系统。

## 技术栈

| 层次 | 技术 | 版本 |
| --- | --- | --- |
| 后端 | Spring Boot | 4.1.2 |
| 后端 | MyBatis-Plus | 3.5.18（boot4 starter） |
| 后端 | MySQL | 8.0 |
| 后端 | JDK | 21（本机用 25 也能编译运行） |
| 前端 | Vue 3 + Vite | 待组员 B 搭建 |
| 前端 | Element Plus + Axios | 待组员 B 搭建 |
| 工具 | IDEA / HeidiSQL / Git / Gitee | — |

> 版本注意：Spring Boot 4 的 Web starter 叫 `spring-boot-starter-webmvc`；MyBatis-Plus 3.5.18 的 `IService` / `ServiceImpl` 包名是 `com.baomidou.mybatisplus.spring.service`，旧教程里的 `...extension.service` 会编译不过。

## 已实现

- 员工列表（连表带部门名称）
- 员工分页 + 姓名/工号关键字搜索
- 员工详情
- 统一返回结构 `Result<T>` / `PageResult<T>`
- MyBatis-Plus 分页插件、驼峰映射、SQL 控制台日志
- 本地联调 CORS
- 数据库初始化脚本、接口契约、团队任务书

## 目录结构

```text
salary-system/
├── src/main/java/com/example/salarysystem/
│   ├── SalarySystemApplication.java
│   ├── common/                 # Result、PageResult
│   ├── config/                 # MyBatis-Plus 配置
│   ├── controller/             # EmployeeController
│   ├── entity/                 # SysEmployee
│   ├── mapper/                 # SysEmployeeMapper
│   ├── service/                # 接口 + impl
│   └── vo/                     # EmployeeVO
├── src/main/resources/
│   └── application.yaml
├── docs/
│   ├── init.sql                # 建库、建表、测试数据
│   ├── api.md                  # 前后端接口契约
│   ├── team-tasks.md           # 三名组员的任务书
│   └── roadmap.md              # 组长进阶路线
├── pom.xml
└── README.md
```

## 快速开始

### 1. 初始化数据库

> `docs/init.sql` 会先删除三张表再重建，只在本地测试库执行。

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

启动成功后访问：

```text
http://localhost:8080/api/employees
http://localhost:8080/api/employees/page?current=1&size=10&keyword=张
```

### 4. 启动前端（组员 B 完成后）

```bash
cd frontend
npm install
npm run dev
```

默认地址 `http://localhost:5173`。

## 接口

完整字段、请求/响应示例、错误码见 [docs/api.md](docs/api.md)。

| 方法 | 路径 | 说明 | 状态 |
| --- | --- | --- | --- |
| GET | `/api/employees` | 全部员工（带部门名） | 已完成 |
| GET | `/api/employees/page` | 分页 + 关键字搜索 | 已完成 |
| GET | `/api/employees/{id}` | 员工详情 | 已完成 |
| POST | `/api/employees` | 新增员工 | 组员 A |
| PUT | `/api/employees/{id}` | 修改员工 | 组员 A |
| DELETE | `/api/employees/{id}` | 删除/离职 | 组员 A |
| GET | `/api/departments` | 部门下拉 | 组员 A |

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

## 文档

- [接口契约](docs/api.md)
- [数据库初始化脚本](docs/init.sql)
- [组员任务书](docs/team-tasks.md)
- [组长进阶路线](docs/roadmap.md)
