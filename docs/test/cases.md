# 薪资模块测试用例（组员 C · 姜锋）

> 自动化用例见 `src/test/java/com/example/salarysystem/SalaryApiTests.java`，
> 基于 H2 内存库（MySQL 模式）+ MockMvc 启动真实 Controller / Service / Mapper 链路。
> 手工接口用例可用 Postman / Apifox 导入 `docs/postman` 后按本表执行，baseUrl=`http://localhost:8080`。

## 1. 测试数据（salary-test.sql）

| 员工 | 工号 | 部门 | 状态 | 月份 | 基本工资 | 奖金 | 扣款 | 实发 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 张三 | E001 | 技术部 | 在职 | 2024-09 | 8000.00 | 1000.00 | 500.00 | 8500.00 |
| 李四 | E002 | 财务部 | 在职 | 2024-09 | 7000.00 | 500.00 | 0.00 | 7500.00 |
| 张五 | E003 | 未分配 | 离职 | 2024-09 | 9000.00 | 2000.00 | 200.00 | 10800.00 |

## 2. 功能用例

| 编号 | 用例 | 输入 / 操作 | 预期结果 | 自动化覆盖 |
| --- | --- | --- | --- | --- |
| SC-01 | 新增薪资并由后端重算实发 | POST `/api/salaries`，empId=1，month=2024-10，base=6000，bonus=100，deduction=100 | code=200，actualPay=6000.00；列表按月份可查到 | createRecalculatesActualPayAndIsQueryable |
| SC-02 | 同员工同月重复新增 | empId=1，month=2024-09 再次提交 | code=400，提示“该员工 2024-09 月已存在薪资记录”，库内不新增 | duplicateMonthFailsWithoutInserting |
| SC-03 | 基本工资为负 | basePay=-100 | code=400，不写库 | invalidCreateReturnsBusiness400AndDoesNotWrite |
| SC-04 | 扣款超过收入 | base=1000，bonus=0，deduction=2000 | code=400，提示扣款不能大于基本工资与奖金之和，不写库 | 同上 |
| SC-05 | 月份格式错误 | salaryMonth=2024/10 | code=400，月份格式应为 yyyy-MM | 同上 |
| SC-06 | 引用不存在员工 | empId=999 | code=400，所选员工不存在，不写库 | referenceToMissingEmployeeReturns400 |
| SC-07 | 奖金为负 | bonus=-50 | code=400，不写库 | invalidCreate... |
| SC-08 | 请求体非法 / 为空 | `{}`、`{broken` | code=400，不写库 | invalidCreate... |
| SC-09 | 修改薪资并重算实发 | PUT `/api/salaries/1`，base=8000，bonus=2000，deduction=1000 | code=200，actualPay=9000.00；详情一致 | updateRecalculatesActualPay |
| SC-10 | 修改时与他人同月重复 | PUT `/api/salaries/2` 改为 empId=1、2024-09 | code=400，原记录实发仍为 7500.00 | updateDuplicateMonthDoesNotChangeRecord |
| SC-11 | 读取 / 修改 / 删除不存在记录 | id=999 | 均返回 code=404 | missingSalaryReturns404ForReadUpdateAndDelete |
| SC-12 | 删除薪资记录 | DELETE `/api/salaries/3` | code=200；再查详情 404；记录数减一 | deleteRemovesRecord |
| SC-13 | 按月份筛选列表 | GET `/api/salaries?salaryMonth=2024-09` | 返回 3 条 | listFiltersByMonthAndEmployee |
| SC-14 | 按员工筛选列表 | GET `/api/salaries?empId=1` | 返回 1 条，工号 E001 | 同上 |
| SC-15 | 分页边界兜底 | current=0，size=1000 | current=1，size=100，total=3 | pageAppliesBoundaryDefaults |

## 3. 计算口径核对（初始三条）

| 员工 | 计算式 | 期望实发 | 库内实发 | 结论 |
| --- | --- | --- | --- | --- |
| 张三 | 8000 + 1000 − 500 | 8500.00 | 8500.00 | 通过 |
| 李四 | 7000 + 500 − 0 | 7500.00 | 7500.00 | 通过 |
| 张五 | 9000 + 2000 − 200 | 10800.00 | 10800.00 | 通过 |

## 4. 待联调补充

- 前端薪资页面联调后，补充页面级用例：金额输入框禁用浮点数、月份控件默认当月、删除二次确认。
- 接入 JWT 后补充未登录访问 `/api/salaries/**` 返回 401 的用例。
