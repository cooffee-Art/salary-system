# 员工薪资管理系统 · 接口契约 v1

> 前后端以本文件为准。字段名、错误码、返回结构一旦确定，任何一方要改都必须先在群里说一声，并同步修改本文件。
>
> 后端地址：`http://localhost:8080`
> 前端开发地址：`http://localhost:5173`

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

## 4. 待实现接口（组员 A 负责）

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

### 4.2 修改员工

`PUT /api/employees/{id}`

请求体同新增（`empNo` 不建议修改）。成功返回 `code=200`。

### 4.3 删除员工

`DELETE /api/employees/{id}`

建议做“逻辑删除”或“离职处理”（把 status 改为 0），不要直接物理删除有薪资记录的员工，否则会触发外键约束。

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

## 5. 联调约定

1. 前端 `.env.development` 里配置 `VITE_API_BASE_URL=http://localhost:8080`，或直接用 Vite 代理 `/api` → `http://localhost:8080`。
2. 后端已在 `EmployeeController` 上加了 `@CrossOrigin`，允许 `localhost` 任意端口访问；如果浏览器仍报 CORS，先检查后端是否重启。
3. 日期统一传 `yyyy-MM-dd` 字符串，不要传时间戳。
4. 金额统一用 `decimal` / `BigDecimal`，前端不要用浮点数做加减。
5. 接口有变动，先改本文件，再改代码，最后在群里通知。
