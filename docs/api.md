# 员工薪资管理系统 · 接口契约 v1.1

> 前后端以本文件为准。字段名、错误码、返回结构一旦确定，任何一方要改都必须先在群里说一声，并同步修改本文件。
>
> 后端地址：`http://localhost:8080`
> 前端开发地址：`http://localhost:5173`
>
> 员工接口的规范地址是 `/api/employees`；旧地址 `/employees` 仍然兼容，但新代码请统一使用带 `/api` 的地址。

## 1. 统一返回结构

所有接口都返回 `Result<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 200 成功；400 参数错误；401 未登录；404 不存在；500 服务器异常 |
| message | string | 给用户看的提示语 |
| data | any | 业务数据，失败时为 null |

> 当前业务接口统一返回 HTTP 200，前端以 body 里的 `code` 为准；后续接入 JWT 后，401 会单独走安全链处理。
> JSON 字段用驼峰（`empNo`），数据库字段用下划线（`emp_no`），由 MyBatis-Plus 自动映射。

分页接口的 `data` 是 `PageResult<T>`：

```json
{
  "records": [],
  "total": 3,
  "current": 1,
  "size": 10,
  "pages": 1
}
```

## 2. 员工字段字典

| 字段 | 类型 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| id | int | 否 | 主键，新增时不需要传 |
| empNo | string | 是 | 工号，唯一，如 E001 |
| name | string | 是 | 姓名 |
| gender | string | 否 | 男 / 女 |
| phone | string | 否 | 手机号 |
| deptId | int | 否 | 部门 ID |
| deptName | string | 否 | 部门名称，只有列表接口返回，新增/修改不传 |
| hireDate | string | 否 | 入职日期，格式 yyyy-MM-dd |
| status | int | 否 | 1=在职，0=离职，默认 1 |

## 3. 已实现接口

### 3.1 查询全部员工

`GET /api/employees`

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "empNo": "E001",
      "name": "张三",
      "gender": "男",
      "phone": "13800138000",
      "deptId": 1,
      "deptName": "技术部",
      "hireDate": "2023-01-15",
      "status": 1
    }
  ]
}
```

### 3.2 分页查询员工

`GET /api/employees/page?current=1&size=10&keyword=张`

| 参数 | 必填 | 默认 | 说明 |
| --- | --- | --- | --- |
| current | 否 | 1 | 页码 |
| size | 否 | 10 | 每页条数，最大 100 |
| keyword | 否 | 空 | 按姓名或工号模糊搜索 |
| deptId | 否 | 空 | 按部门 ID 精确筛选，必须为正整数 |
| deptName | 否 | 空 | 按部门名称模糊搜索 |

三个筛选条件同时传入时使用 AND 组合；空白 keyword、deptName 等同不筛选。
例如：`GET /api/employees/page?current=1&size=10&keyword=张&deptName=技术`。
current 小于 1 时按 1 处理，size 限制在 1–100；超出最后一页返回空 records。
未传筛选条件时仍包含未分配部门和离职员工，兼容原有调用。

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "empNo": "E001",
        "name": "张三",
        "gender": "男",
        "phone": "13800138000",
        "deptId": 1,
        "deptName": "技术部",
        "hireDate": "2023-01-15",
        "status": 1
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1
  }
}
```

### 3.3 查询员工详情

`GET /api/employees/{id}`

响应 `data` 为员工对象（不含 `deptName`）。不存在时：

```json
{
  "code": 404,
  "message": "员工不存在",
  "data": null
}
```

## 4. 员工写入与部门下拉接口（已实现）

### 4.1 新增员工

`POST /api/employees`

请求体：

```json
{
  "empNo": "E004",
  "name": "赵六",
  "gender": "男",
  "phone": "13600136000",
  "deptId": 1,
  "hireDate": "2024-05-10",
  "status": 1
}
```

成功返回 `code=200`，`data` 为新增后的员工（含自增 id）。

校验约定（新增与修改共用）：

- empNo、name 必填，去除首尾空白后分别最多 20、50 个字符；工号全表唯一。
- gender 仅允许男、女；phone 若填写，须符合 `^1[3-9]\d{9}$`。
- gender、phone 可传 null 或空白字符串，空白会转为 null。
- deptId 可为 null；填写时必须为正整数，且部门存在。
- hireDate 可为 null；填写时为有效的 `yyyy-MM-dd` 日期，且不能晚于今天。
- status 只能为 0 或 1，省略或传 null 时默认 1。
- 请求 DTO 不包含 id、deptName；前端不应传入这些字段。

例如重复工号返回（HTTP 状态仍为 200）：

```json
{"code":400,"message":"工号已存在","data":null}
```

### 4.2 修改员工

`PUT /api/employees/{id}`

请求体同新增，采用完整替换语义，empNo、name 每次必传。
允许修改工号，但不能与其他员工重复；保留原工号时不会误报重复。
可选字段省略或传 null 会清空，status 省略或传 null 时设为 1。
成功返回 `code=200`，data 为修改后的员工对象；不存在返回业务码 404。

示例：清空员工的部门、手机号、性别及入职日期，同时设为离职。

```json
{"empNo":"E004","name":"赵六","status":0}
```

### 4.3 删除员工

`DELETE /api/employees/{id}`

- 有薪资记录：设置 status=0，保留员工和薪资历史，返回：
  `{"code":200,"message":"操作成功","data":"已转为离职，保留薪资记录"}`。
- 无薪资记录：物理删除，返回：
  `{"code":200,"message":"操作成功","data":"员工已删除"}`。
- 员工不存在：业务码 404；已离职且有薪资的员工重复调用仍然成功。
- 离职员工仍可被列表和详情查询到，前端应刷新列表并展示状态。
- 检查薪资与删除在同一事务内执行，并锁定员工行。

### 4.4 部门下拉列表

`GET /api/departments`

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "id": 1, "deptName": "技术部" },
    { "id": 2, "deptName": "财务部" },
    { "id": 3, "deptName": "人事部" }
  ]
}
```

仅返回 id、deptName，按 id 升序；没有部门时返回空数组。
本接口只提供员工表单下拉数据，部门增删改接口仍由部门模块负责。

### 4.5 错误返回

员工 ID 必须为正整数；非法 ID、错误数字/日期格式、空请求体、校验失败、
重复工号和无效部门均返回业务码 400，data 为 null。
不存在的员工返回业务码 404。业务失败沿用 HTTP 200 + body.code 契约。
员工、部门、登录、薪资模块共用全局异常处理器。

## 5. 登录鉴权

### 5.1 登录

`POST /api/auth/login`

请求体：

```json
{
  "username": "admin",
  "password": "admin123"
}
```

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "username": "admin",
    "role": "ADMIN"
  }
}
```

默认本地账号：`admin / admin123`，仅用于课堂练习，上线前必须修改。

### 5.2 当前登录人

`GET /api/auth/me`

请求头：

```text
Authorization: Bearer <token>
```

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "username": "admin",
    "role": "ADMIN"
  }
}
```

### 5.3 鉴权约定

- 除 `POST /api/auth/login` 外，所有 `/api/**` 都需要 `Authorization: Bearer <token>`。
- 未携带或 Token 无效：HTTP 401，body 为 `{"code":401,"message":"未登录或登录已过期","data":null}`。
- Token 默认 2 小时过期；过期后重新登录。
- 角色字段为 `ADMIN / HR / EMPLOYEE`，后续可在此基础上做接口级权限。

## 6. 薪资接口

### 6.1 薪资分页

`GET /api/salaries/page?month=2024-09&keyword=张&current=1&size=10`

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| month | 否 | `yyyy-MM`，不传查全部月份 |
| keyword | 否 | 员工姓名或工号模糊搜索 |
| current | 否 | 页码，默认 1 |
| size | 否 | 每页条数，默认 10，最大 100 |

响应 `data.records` 中每条记录包含：

```json
{
  "id": 1,
  "empId": 1,
  "empNo": "E001",
  "empName": "张三",
  "deptId": 1,
  "deptName": "技术部",
  "salaryMonth": "2024-09",
  "basePay": 8000.00,
  "bonus": 1000.00,
  "deduction": 500.00,
  "actualPay": 8500.00,
  "createTime": "2026-10-28 21:17:16"
}
```

### 6.2 部门月度薪资汇总

`GET /api/salaries/report/department?month=2024-09`

响应 `data` 为数组：

```json
[
  {
    "deptId": 1,
    "deptName": "技术部",
    "salaryMonth": "2024-09",
    "empCount": 2,
    "totalBase": 17000.00,
    "totalBonus": 3000.00,
    "totalDeduction": 700.00,
    "totalActual": 19300.00
  }
]
```

没有薪资记录的月份返回空数组。

### 6.3 生成月度工资

`POST /api/salaries/generate?month=2024-10`

规则：

- 只处理 `status=1` 的在职员工。
- 以员工在该月之前最近一条薪资的 `base_pay` 作为本月基本工资。
- 奖金、扣款默认为 0；`actual_pay = base_pay`。
- 没有历史薪资的员工计入 `skipped`，不会生成 0 元工资。
- 同一月份重复生成返回业务码 400。
- 生成过程使用事务；唯一约束 `uk_emp_month(emp_id, salary_month)` 防止并发重复。

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "month": "2024-10",
    "generated": 3,
    "skipped": 0,
    "totalActual": 24000.00
  }
}
```

## 7. 联调约定

1. 前端 `.env.development` 里配置 `VITE_API_BASE_URL=http://localhost:8080`，或直接用 Vite 代理 `/api` → `http://localhost:8080`。
2. 员工与部门 Controller 已加本地 CORS，允许 `localhost` / `127.0.0.1` 任意端口访问；如果浏览器仍报 CORS，先检查后端是否重启。
3. 日期统一传 `yyyy-MM-dd` 字符串，不要传时间戳。
4. 金额统一用 `decimal` / `BigDecimal`，前端不要用浮点数做加减。
5. 接口有变动，先改本文件，再改代码，最后在群里通知。
6. 可导入 `docs/postman/employee-management.postman_collection.json` 进行员工 CRUD 联调；用法与验证结果见 `docs/employee-development.md`。
