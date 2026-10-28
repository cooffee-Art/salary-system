# 组员任务书（可直接复制到微信群）

> 用法：下面三个人物块分别对应组员 A / B / C。复制时只复制对应代码块里的内容，把 `<仓库地址>` 换成你们的 Gitee 地址即可。
>
> 发之前组长先做三件事：
> 1. 把 `docs/api.md`、`docs/init.sql` 推到 Gitee；
> 2. 建好 `dev` 分支，并把三名组员加成仓库开发者；
> 3. 在群里先发一条“分支规范 + 每天 21:30 站会”的群公告。

---

## 0. 先立规矩（组长自己留着，不用发）

| 事项 | 约定 |
| --- | --- |
| 分支 | `main` 只放可演示版本；`dev` 日常集成；每人 `feature/xxx` |
| 合并 | 个人分支 PR 到 `dev`，组长 review 后合并；`main` 只由组长从 `dev` 合并 |
| 站会 | 每天 21:30，15 分钟，每人三句话：昨天做了什么、今天做什么、卡在哪里 |
| 提交 | `feat(模块): ...` / `fix(模块): ...` / `docs: ...` |
| 禁止 | 提交 `target/`、`.idea/`、`node_modules/`、真实密码 |
| 接口 | 以 `docs/api.md` 为准，改字段先群里说 |
| 完成 | 本地能跑 + 自测通过 + 文档更新 + PR 已提 |
| 技术栈 | Spring Boot 4.1.2 + MyBatis-Plus 3.5.18 + MySQL 8.0 + JDK 21（本机 25 也可） |

常用命令：

```bash
git clone <仓库地址>
git checkout dev
git pull origin dev
git checkout -b feature/xxx

# 开发完
git add .
git commit -m "feat(employee): 完成员工新增接口"
git push -u origin feature/xxx
```

---

## 1. 组员 A · 后端开发

### 任务定位

在组长搭好的 MyBatis-Plus 骨架上，完成员工模块 CRUD 和部门下拉接口。这是整个系统的地基，做完这一轮，MyBatis-Plus 的单表操作你就真正入门了。

### 微信复制版

```text
Hi A，这周你的主战场是后端员工模块的 CRUD。组长已经把 MyBatis-Plus 的骨架搭好了，员工列表和分页查询已经能跑通，你接下来是在这个基础上把“增删改查”补齐。放心，不是从零开始。

一、先拉代码（10 分钟）
1. 克隆并切到 dev：
   git clone <仓库地址>
   git checkout dev
   git pull origin dev
2. 新建自己的分支：
   git checkout -b feature/employee-crud
3. 用 IDEA 打开项目，等 Maven 依赖下载完；启动 SalarySystemApplication，浏览器访问 http://localhost:8080/api/employees，能看到 3 条员工数据再往下做。
4. 先读两个文件：docs/api.md（接口契约）、src/main/java/com/example/salarysystem/controller/EmployeeController.java（代码风格）。

二、你要实现的接口（字段以 docs/api.md 为准）
1. POST /api/employees 新增员工
   - 必填：empNo、name；empNo 不能重复
   - 选填：gender、phone、deptId、hireDate、status
   - 用 @Valid + DTO 接参数，不要直接拿实体接收
2. PUT /api/employees/{id} 修改员工
   - id 不存在返回 code=404
3. DELETE /api/employees/{id} 删除员工
   - 如果该员工已有薪资记录，不要物理删除，改成 status=0（离职）
   - 返回统一 Result
4. GET /api/departments 部门下拉列表
   - 新建 SysDepartment 实体 / Mapper / Service / Controller
   - 返回 id、deptName 两个字段即可

三、代码要求（照着现有 EmployeeController 的风格写）
- 项目是 Spring Boot 4.1.2 + MyBatis-Plus 3.5.18；IService / ServiceImpl 的包名是 com.baomidou.mybatisplus.spring.service...，别照旧教程导入 extension.service
- Controller 只做参数接收和结果返回，业务写在 Service
- 所有接口返回 Result<T>，不要返回裸对象
- 新增/修改用 DTO（建议 EmployeeSaveDTO），实体只做数据库映射
- 参数校验注解：@NotBlank、@Pattern(regexp = "^1[3-9]\\d{9}$")、@PastOrPresent
- 新 Controller 记得加 @CrossOrigin，或者问组长要不要提成全局 CORS
- 不要自己写 try/catch 包一层 Result，统一异常处理组长这周会补
- 每写完一个接口，用 Apifox/Postman 自测，把请求示例发群里

四、自测清单（必须全过）
- 正常新增 -> code=200，列表能查到
- 重复工号 -> 返回明确错误，不能抛 500 堆栈
- 修改不存在的 id -> code=404
- 删除有薪资记录的员工 -> 不报外键错误（走离职）
- 手机号格式错误 -> 返回参数错误
- 分页 current=0、size=1000 -> 不报错，有兜底

五、时间节点
- 第 1 天：拉代码 + 部门接口 + DTO 设计
- 第 2 天：新增 / 修改 / 删除
- 第 3 天：自测 + 提 PR 到 dev，并在群里 @ 组长

六、交付
- 分支 feature/employee-crud
- PR 标题：feat(employee): 完成员工 CRUD 与部门下拉
- PR 描述里贴 4 个接口的请求 / 响应示例
- 如果字段有变动，同步更新 docs/api.md

有卡住超过 30 分钟的问题，直接在群里发“现象 + 报错 + 已尝试”，别自己硬扛。你负责的这块是整个系统的地基，做完这轮，MyBatis-Plus 的 CRUD 你就真正入门了。
```

### 组长验收要点

- [ ] `POST` 重复工号返回业务错误，不是 500
- [ ] `PUT` 不存在的 id 返回 404
- [ ] `DELETE` 有薪资记录的员工不触发外键异常
- [ ] 分页边界值不报错
- [ ] DTO 上有校验注解，Controller 不直接收实体
- [ ] 新接口全部返回 `Result<T>`
- [ ] `docs/api.md` 与实际返回一致

---

## 2. 组员 B · 前端主力

### 任务定位

从零搭 Vue 3 + Vite + Element Plus，先把员工管理页面用 Mock 数据完整画出来，再和后端联调。前端是项目的“门面”，页面完成度直接决定答辩观感。

### 微信复制版

```text
Hi B，前端就交给你了，你是我们组的“门面担当”。这周的目标只有一个：把员工管理页面完整画出来，并且用假数据先跑通，不用等后端。

一、拉代码 + 起项目
1. git clone <仓库地址>
   git checkout dev
   git checkout -b feature/frontend-employee
2. 在项目根目录建 frontend/：
   npm create vite@latest frontend -- --template vue
   cd frontend
   npm install
   npm install element-plus axios vue-router @element-plus/icons-vue
   npm run dev
3. 浏览器打开 http://localhost:5173，看到 Vite 默认页就算成功。

二、接口地址（先记下来，不急着联调）
- 后端：http://localhost:8080
- 员工列表：GET /api/employees
- 分页：GET /api/employees/page?current=1&size=10&keyword=张
- 部门：GET /api/departments（A 在写，先用假数据）
- 完整字段看 docs/api.md
- 后端已开 CORS；建议在 vite.config.js 配代理，前端请求写 /api/...，避免跨域：
  server: {
    port: 5173,
    proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } }
  }

三、页面要求（先 Mock，后联调）
1. 整体布局：左侧菜单（员工管理、薪资管理、部门管理），顶部标题 / 用户区
2. 员工管理页：
   - 搜索栏：姓名 / 工号关键字 + 查询 + 重置
   - 表格列：工号、姓名、性别、手机号、部门、入职日期、状态、操作
   - 分页：每页 10 条，可切页
   - 新增 / 编辑弹窗：工号、姓名、性别（下拉）、手机号、部门（下拉）、入职日期（日期选择）、状态（开关）
   - 删除：二次确认；有薪资记录的员工提示“改为离职”
   - 状态用 el-tag：在职绿色、离职灰色
3. 交互细节：loading、空数据、接口失败提示、表单校验（工号 / 姓名必填、手机号正则）
4. 先写 src/mock/employees.js，造 8-10 条数据；在 src/api/employee.js 里加一个 USE_MOCK 开关，默认 true。这样后端没好的时候你也能开发。

四、建议目录
frontend/src/
  api/employee.js
  mock/employees.js
  views/EmployeeList.vue
  components/EmployeeFormDialog.vue
  router/index.js
  App.vue
  main.js

五、联调顺序
1. 第 1 天：Vite + Element Plus + 布局 + 表格（Mock）
2. 第 2 天：搜索、分页、新增 / 编辑弹窗、删除确认（Mock）
3. 第 3 天：等 A 的接口好了，把 USE_MOCK 改 false，逐个接口联调；把问题记下来发群里

六、验收标准
- npm run dev 能启动，控制台无红色报错
- 表格、搜索、分页、弹窗都能用
- 新增 / 编辑 / 删除在 Mock 下完整走通
- 联调后刷新页面数据仍在（说明走的是真接口）
- 手机号、必填项校验有提示
- 提交 PR 到 dev，附一张页面截图

七、时间节点
第 3 天晚上前把 PR 提到 dev。

你不用等后端，也不要因为“接口还没好”就停。先把页面和交互做到 80 分，联调时只改 API 层，这就是前端工程化的价值。有 UI 想法随时发群里，我们一起定。
```

### 组长验收要点

- [ ] `frontend/` 能独立启动，README 里补一段前端启动说明
- [ ] 表格字段与 `docs/api.md` 一致（特别是 `empNo`、`deptName`、`hireDate`）
- [ ] 分页、搜索、重置、新增、编辑、删除都能操作
- [ ] Mock 与真实接口切换只改一个开关
- [ ] 手机号、必填项有校验
- [ ] 控制台无报错，页面在 1366 宽度下不横向溢出

---

## 3. 组员 C · 数据库 + 测试

### 任务定位

数据库已经有初稿，你的任务不是重复建表，而是评审、加固、补测试数据，并每天做接口验收。你是质量守门员，Bug 早发现一天，组长就少熬一晚。

### 微信复制版

```text
Hi C，你负责数据库和测试，这个角色其实是“质量守门员”。先同步一个情况：库里 sys_department、sys_salary 已经有一版初稿了（我看了 salary_db），所以你的任务不是从零再建一遍，而是把这版结构评审、加固、补数据，并且每天验收接口。这比单纯写建表 SQL 更能体现你的价值。

一、拉代码
git clone <仓库地址>
git checkout dev
git checkout -b feature/db-review

二、数据库现状（HeidiSQL 里确认）
- sys_department：id、dept_name、create_time
- sys_employee：id、emp_no、name、gender、phone、dept_id、hire_date、status
- sys_salary：id、emp_id、salary_month、base_pay、bonus、deduction、actual_pay、create_time
- 关系：部门 1:N 员工，员工 1:N 薪资

三、你要做的四件事
1. ER 图 + 字段评审（第 1 天）
   - 用 draw.io / ProcessOn 画三张表的关系图，标出主键、外键、1:N
   - 检查：主键自增、emp_no 唯一、外键是否生效、金额 decimal(10,2)、status 默认 1、create_time 默认当前时间
   - 重点：sys_salary 目前没有“同一员工同一月份唯一”的约束，评估是否加 uk_emp_month(emp_id, salary_month)，把结论写进 docs/db-review.md
2. 建表 + 测试数据 SQL（第 2 天）
   - 以 docs/init.sql 为基线，核对是否和现库一致；有修改就更新它
   - 在本地新建 salary_db_test 库跑一遍，确保从零能建成功
   - 造测试数据：5 个部门、10 个员工、20 条薪资（覆盖 2 个月；包含无部门、离职、奖金为 0、扣款大于奖金等边界）
   - 不要直接动 salary_db 的正式数据，先在测试库验证
3. 每日接口验收（每天 21:00）
   - 用 Apifox / Postman 建一个“员工薪资系统”集合，环境变量 baseUrl=http://localhost:8080
   - 按 docs/api.md 逐个跑：
     GET /api/employees
     GET /api/employees/page（正常、keyword、current=0、size=1000）
     GET /api/employees/{id}（存在 / 不存在）
     POST / PUT / DELETE（A 做好后）
     GET /api/departments
   - 重点测：重复工号、删除有薪资的员工、日期格式、手机号非法、keyword 传 ' OR 1=1 --
   - 发现问题记录到 docs/test/bugs.md：编号、严重级别、复现步骤、期望 / 实际、截图、负责人
4. 每天 21:30 在群里发验收报告
   格式：
   【今日验收】
   通过：x/y
   阻塞：...
   新 Bug：...
   风险：...
   明日重点：...

四、交付物
- docs/db-review.md（ER 图 + 评审结论 + 索引建议）
- docs/init.sql（可重复执行的初始化脚本，与 C 的评审结论一致）
- docs/test/cases.md（测试用例）
- docs/test/bugs.md（Bug 清单）
- 每天的验收报告

五、验收标准
- init.sql 在空库上一次执行成功，外键、唯一约束生效
- 测试数据覆盖正常 + 边界场景
- 所有已实现接口有测试记录
- Bug 有闭环（修复后回归通过）

六、时间节点
- 第 1 天：ER 图 + 评审
- 第 2 天：init.sql + 测试数据 + 用例
- 第 3 天：全量回归 + 输出报告

C，你这个位置最容易被动“等别人写完再测”。主动一点：A/B 每提一个 PR，你就先跑一遍冒烟，问题早发现一天，组长就少熬一晚。数据库的约束和测试用例，是这个项目最后能拿得出手的底气。
```

### 组长验收要点

- [ ] ER 图能讲清楚三张表的关系
- [ ] `init.sql` 在空库一次执行成功
- [ ] `emp_no` 唯一、外键有效、金额精度正确
- [ ] 是否加 `uk_emp_month` 有明确结论（加或不加都要有理由）
- [ ] 测试数据覆盖边界场景
- [ ] 每日验收报告有截断、有结论、有负责人

---

## 4. 组长转发后的跟进节奏

| 时间 | 动作 |
| --- | --- |
| 第 1 天上午 | 发任务书 + 建 `dev` 分支 + 确认三人都能拉代码启动 |
| 第 1 天晚上 | 检查 A 的部门接口、B 的页面骨架、C 的 ER 图 |
| 第 2 天晚上 | 检查 A 的 CRUD、B 的 Mock 交互、C 的测试数据 |
| 第 3 天晚上 | 三人 PR 合并到 `dev`，组长跑一遍全量冒烟 |
| 第 4 天 | 组长补齐全局异常 / JWT / 报表，准备答辩演示 |

> 提醒：任务书发出去后，组长不要做“传话筒”。每天站会只问三个问题：昨天做了什么、今天做什么、卡在哪里。卡住超过 30 分钟的问题，必须发“现象 + 报错 + 已尝试”，不允许只发“跑不起来”。
