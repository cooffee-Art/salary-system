import request from './request'

/**
 * 薪资模块 API（真实后端）
 *
 * 列表：GET /api/salaries/page?month=YYYY-MM&keyword=&current=&size=
 * 详情：GET /api/salaries/{id}
 * 新增：POST /api/salaries
 * 修改：PUT /api/salaries/{id}
 * 删除：DELETE /api/salaries/{id}
 * 部门报表：GET /api/salaries/report/department?month=YYYY-MM
 * 生成工资：POST /api/salaries/generate?month=YYYY-MM
 */

export function fetchSalaryPage(params) {
  return request.get('/salaries/page', { params })
}

export function fetchSalaryDetail(id) {
  return request.get(`/salaries/${id}`)
}

export function createSalary(payload) {
  return request.post('/salaries', payload)
}

export function updateSalary(id, payload) {
  return request.put(`/salaries/${id}`, payload)
}

export function deleteSalary(id) {
  return request.delete(`/salaries/${id}`)
}

/** 部门月度报表：data 为 DeptSalaryReportVO[] */
export function fetchDepartmentReport(month) {
  return request.get('/salaries/report/department', { params: { month } })
}

/**
 * 生成某月工资。
 * raw: true → 拿完整 { code, message, data }，便于展示“该月工资已生成”这类后端提示。
 * 返回 data：{ generated, skipped, totalActual }
 */
export function generateMonthlySalary(month) {
  return request.post('/salaries/generate', null, {
    params: { month },
    raw: true,
  })
}
