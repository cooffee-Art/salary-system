# 测试用例

> 自动化测试：
> - `AuthApiTests`：登录、401、Token 访问
> - `EmployeeApiTests`：员工模块
> - `SalaryApiTests`：薪资分页、部门报表、月度生成
> - `SalaryCrudApiTests`：薪资 CRUD、金额计算、唯一约束
>
> 手工接口用例可用 Postman / Apifox，baseUrl=`http://localhost:8080`。

## 1. 登录鉴权

| 编号 | 用例 | 操作 | 预期 |
| --- | --- | --- | --- |
| AU-01 | 正确登录 | POST `/api/auth/login`，admin/admin123 | code=200，data.token 非空，role=ADMIN |
| AU-02 | 错误密码 | POST 登录，密码错误 | code=401，提示“用户名或密码错误” |
| AU-03 | 未带 Token 访问员工 | GET `/api/employees` | HTTP 401，body.code=401 |
| AU-04 | 未带 Token 访问薪资 | GET `/api/salaries/page` | HTTP 401，body.code=401 |
| AU-05 | 带 Token 访问 | 登录后加 `Authorization: Bearer <token>` | code=200 |
| AU-06 | 当前登录人 | GET `/api/auth/me` | data.username=admin |

## 2. 员工模块

| 编号 | 用例 | 操作 | 预期 |
| --- | --- | --- | --- |
| EM-01 | 列表 | GET `/api/employees` | 3 条，含 deptName |
| EM-02 | 分页 + 关键字 | GET `/api/employees/page?keyword=张` | total=1 |
| EM-03 | 部门筛选 | deptId/deptName 组合 | 只返回对应部门 |
| EM-04 | 新增 | POST 合法员工 | code=200，列表可查 |
| EM-05 | 重复工号 | POST 已存在 empNo | code=400，提示“工号已存在” |
| EM-06 | 非法手机号 | phone=123 | code=400 |
| EM-07 | 未来入职日期 | hireDate=2999-01-01 | code=400 |
| EM-08 | 部门不存在 | deptId=999 | code=400 |
| EM-09 | 修改不存在 ID | PUT `/api/employees/999` | code=404 |
| EM-10 | 删除无薪资员工 | DELETE | 物理删除，data=“员工已删除” |
| EM-11 | 删除有薪资员工 | DELETE | 转离职，data=“已转为离职，保留薪资记录” |
| EM-12 | 分页边界 | current=0&size=1000 | current=1，size=100 |
| EM-13 | SQL 注入 | keyword=`' OR 1=1 --` | total=0，不报错 |

## 3. 薪资 CRUD（SalaryCrudApiTests）

| 编号 | 用例 | 操作 | 预期 |
| --- | --- | --- | --- |
| SC-01 | 新增并重算实发 | empId=1，2024-10，base=6000，bonus=100，deduction=100 | code=200，actualPay=6000.00 |
| SC-02 | 同员工同月重复 | 再次提交 2024-09 | code=400，提示已存在薪资记录 |
| SC-03 | 基本工资为负 | basePay=-100 | code=400，不写库 |
| SC-04 | 扣款超过收入 | base=1000，bonus=0，deduction=2000 | code=400 |
| SC-05 | 月份格式错误 | 2024/10 | code=400 |
| SC-06 | 员工不存在 | empId=999 | code=400 |
| SC-07 | 修改并重算 | PUT id=1，base=8000，bonus=2000，deduction=1000 | actualPay=9000.00 |
| SC-08 | 修改为重复月份 | PUT id=2 改成 emp1/2024-09 | code=400，原记录不变 |
| SC-09 | 不存在记录 | GET/PUT/DELETE id=999 | code=404 |
| SC-10 | 删除 | DELETE id=3 | code=200，再查 404 |
| SC-11 | 按月份/员工筛选 | GET `/api/salaries?salaryMonth=2024-09`、`?empId=1` | 3 条 / 1 条 |
| SC-12 | 分页边界 | current=0&size=1000 | current=1，size=100，total=3 |

## 4. 薪资分页 / 报表 / 生成（SalaryApiTests）

| 编号 | 用例 | 操作 | 预期 |
| --- | --- | --- | --- |
| SR-01 | 分页连表 | GET `/api/salaries/page?month=2024-09` | 含 empName/deptName/actualPay |
| SR-02 | 部门报表 | GET `/api/salaries/report/department?month=2024-09` | 部门、人数、合计正确 |
| SR-03 | 生成工资 | POST `/api/salaries/generate?month=2024-10` | generated=1，skipped=1（测试数据） |
| SR-04 | 重复生成 | 再次生成同一月份 | code=400，提示已生成 |
| SR-05 | 非法月份 | 2024-13 | code=400 |
| SR-06 | 未来月份 | 2999-01 | code=400 |
| SR-07 | 金额计算 | 生成后 actual_pay=base_pay | 正确 |

## 5. 契约与安全

| 编号 | 用例 | 预期 |
| --- | --- | --- |
| CT-01 | 业务失败 HTTP 状态 | HTTP 200 + body.code（400/404） |
| CT-02 | 未登录 | HTTP 401 + JSON，不是 HTML |
| CT-03 | 响应敏感字段 | 不出现 password |
| CT-04 | CORS 预检 | OPTIONS 返回 200，允许本地端口 |
| CT-05 | 金额类型 | 使用 decimal/BigDecimal，无浮点误差 |
| CT-06 | SQL 注入 | keyword 参数按普通字符串处理 |

## 6. 待人工补充

- 前端页面级用例：登录、员工表格/弹窗、薪资页面、生成按钮二次确认。
- 浏览器 1366 宽度下的布局检查。
- 演示前完整走一遍“登录 → 员工 CRUD → 薪资列表 → 报表 → 生成”。
