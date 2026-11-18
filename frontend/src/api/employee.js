import request from './request'

/**
 * 员工模块 API（真实后端）
 *
 * 列表：GET /api/employees/page?current=&size=&keyword=&deptId=
 * 部门：GET /api/departments
 * 新增：POST /api/employees
 * 修改：PUT /api/employees/{id}
 * 删除：DELETE /api/employees/{id}
 *
 * 注意：request 响应拦截器已经把 { code, message, data } 解包，
 * 所以这里返回的就是 data 本身。
 */

export function fetchEmployeePage(params) {
  return request.get('/employees/page', { params })
}

export function fetchDepartments() {
  return request.get('/departments')
}

/** 全部员工（带 deptName），用于薪资录入时的员工下拉 */
export function fetchAllEmployees() {
  return request.get('/employees')
}

export function createEmployee(payload) {
  return request.post('/employees', payload)
}

export function updateEmployee(id, payload) {
  return request.put(`/employees/${id}`, payload)
}

/** 返回 data：通常是 "员工已删除" 或 "已转为离职，保留薪资记录" */
export function deleteEmployee(id) {
  return request.delete(`/employees/${id}`)
}
