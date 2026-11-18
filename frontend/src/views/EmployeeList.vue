<template>
  <div class="employee-page">
    <el-card class="employee-card" shadow="never">
      <div class="page-header">
        <div class="page-heading">
          <h2 class="page-title">员工管理</h2>
          <p class="page-subtitle">SYS / EMPLOYEE DATABASE</p>
        </div>
        <div class="page-actions">
          <span class="online-badge">
            <span class="dot" aria-hidden="true"></span>
            API CONNECTED
          </span>
        </div>
      </div>

      <!-- 搜索：keyword 查姓名/工号，deptId 精确筛选部门 -->
      <el-form
        class="search-bar"
        :inline="true"
        :model="searchForm"
        @submit.prevent="handleSearch"
      >
        <el-form-item label="关键字">
          <el-input
            v-model="searchForm.keyword"
            placeholder="姓名 / 工号"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-select
            v-model="searchForm.deptId"
            placeholder="全部部门"
            clearable
            style="width: 200px"
            @change="handleSearch"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.deptName"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar">
        <el-button type="primary" @click="openCreate">新增员工</el-button>
      </div>

      <el-table
        v-loading="loading"
        class="employee-table"
        :data="tableData"
        border
        stripe
        row-key="id"
        style="width: 100%"
      >
        <el-table-column prop="empNo" label="工号" width="110" />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="gender" label="性别" width="80" align="center" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="deptName" label="部门" min-width="110" />
        <el-table-column prop="hireDate" label="入职日期" min-width="120" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'info'"
              effect="dark"
              size="small"
            >
              {{ row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无员工数据" :image-size="90" />
        </template>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 新增 / 编辑共用弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      class="cyber-dialog"
      :title="isEdit ? '编辑员工' : '新增员工'"
      width="560px"
      destroy-on-close
      @closed="handleDialogClosed"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="90px">
        <el-form-item label="工号" prop="empNo">
          <el-input v-model="formData.empNo" placeholder="如 E001" maxlength="20" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名" maxlength="50" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="formData.gender" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="11 位手机号（可不填）"
            maxlength="11"
          />
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-select
            v-model="formData.deptId"
            placeholder="请选择部门"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.deptName"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入职日期" prop="hireDate">
          <el-date-picker
            v-model="formData.hireDate"
            type="date"
            placeholder="请选择入职日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledFutureDate"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">在职</el-radio>
            <el-radio :value="0">离职</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import {
  createEmployee,
  deleteEmployee,
  fetchDepartments,
  fetchEmployeePage,
  updateEmployee,
} from '@/api/employee'

/* ------------------------------ 列表状态 ------------------------------ */
const loading = ref(false)
const tableData = ref([])
const departments = ref([])

const searchForm = reactive({
  keyword: '',
  deptId: null,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

/**
 * GET /api/employees/page?current=&size=&keyword=&deptId=
 * 后端返回 data: { records, total, current, size, pages }
 */
async function loadData() {
  loading.value = true
  try {
    const data = await fetchEmployeePage({
      current: pagination.page,
      size: pagination.pageSize,
      keyword: searchForm.keyword.trim() || undefined,
      deptId: searchForm.deptId ?? undefined,
    })

    tableData.value = data?.records ?? []
    pagination.total = Number(data?.total ?? 0)
  } catch {
    // 错误提示已由 request.js 的响应拦截器统一处理
  } finally {
    loading.value = false
  }
}

/** GET /api/departments */
async function loadDepartments() {
  try {
    const data = await fetchDepartments()
    departments.value = Array.isArray(data) ? data : []
  } catch {
    departments.value = []
  }
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.deptId = null
  pagination.page = 1
  loadData()
}

/* --------------------------- 分页（防抖合并） --------------------------- */
let reloadTimer = null

function scheduleReload() {
  clearTimeout(reloadTimer)
  reloadTimer = setTimeout(() => {
    loadData()
  }, 30)
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.page = 1
  scheduleReload()
}

function handleCurrentChange(page) {
  pagination.page = page
  scheduleReload()
}

/* ------------------------------ 弹窗表单 ------------------------------ */
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  empNo: '',
  name: '',
  gender: '男',
  phone: '',
  deptId: null,
  hireDate: '',
  status: 1,
})

const isEdit = computed(() => formData.id !== null && formData.id !== undefined)

function validatePhone(_rule, value, callback) {
  if (!value) return callback()
  if (!/^1[3-9]\d{9}$/.test(value)) {
    return callback(new Error('手机号格式不正确，应为 1 开头的 11 位数字'))
  }
  return callback()
}

function validateHireDate(_rule, value, callback) {
  if (!value) return callback()
  const today = new Date()
  today.setHours(23, 59, 59, 999)
  const selected = new Date(`${value}T00:00:00`)
  if (Number.isNaN(selected.getTime())) {
    return callback(new Error('日期格式不正确'))
  }
  if (selected.getTime() > today.getTime()) {
    return callback(new Error('入职日期不能晚于今天'))
  }
  return callback()
}

const rules = {
  empNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  hireDate: [{ validator: validateHireDate, trigger: 'change' }],
}

function disabledFutureDate(date) {
  const today = new Date()
  today.setHours(23, 59, 59, 999)
  return date.getTime() > today.getTime()
}

function resetFormData() {
  formData.id = null
  formData.empNo = ''
  formData.name = ''
  formData.gender = '男'
  formData.phone = ''
  formData.deptId = null
  formData.hireDate = ''
  formData.status = 1
}

function openCreate() {
  resetFormData()
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEdit(row) {
  Object.assign(formData, {
    id: row.id,
    empNo: row.empNo ?? '',
    name: row.name ?? '',
    gender: row.gender || '男',
    phone: row.phone ?? '',
    deptId: row.deptId ?? null,
    hireDate: row.hireDate ?? '',
    status: row.status ?? 1,
  })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function handleDialogClosed() {
  resetFormData()
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  const payload = {
    empNo: formData.empNo.trim(),
    name: formData.name.trim(),
    gender: formData.gender,
    phone: formData.phone.trim() || null,
    deptId: formData.deptId ?? null,
    hireDate: formData.hireDate || null,
    status: formData.status,
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateEmployee(formData.id, payload)
      ElMessage.success('修改成功')
    } else {
      await createEmployee(payload)
      ElMessage.success('新增成功')
      pagination.page = 1
    }
    dialogVisible.value = false
    await loadData()
  } catch {
    // 后端业务错误（如“工号已存在”“部门不存在”）已由 request.js 的
    // 响应拦截器读出 message 并 ElMessage.error 弹出，这里保持弹窗打开即可
  } finally {
    submitting.value = false
  }
}

/* -------------------------------- 删除 -------------------------------- */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除员工「${row.name}」吗？`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      },
    )
  } catch {
    return
  }

  try {
    // 后端 data 直接是提示语，例如“已转为离职，保留薪资记录”“员工已删除”
    const result = await deleteEmployee(row.id)
    const message = typeof result === 'string' && result.trim() ? result : '删除成功'
    ElMessage.success(message)
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page -= 1
    }
    await loadData()
  } catch {
    // 拦截器已提示（如“员工不存在”）
  }
}

onMounted(() => {
  loadDepartments()
  loadData()
})
</script>

<style scoped>
.employee-page {
  max-width: 1320px;
  margin: 0 auto;
  background: transparent;
}

/* 深色玻璃拟态面板 */
.employee-card {
  position: relative;
  overflow: hidden;
  background: rgba(10, 15, 30, 0.7);
  border: 1px solid rgba(0, 240, 255, 0.3);
  backdrop-filter: blur(10px) saturate(1.2);
  -webkit-backdrop-filter: blur(10px) saturate(1.2);
  box-shadow:
    0 0 30px rgba(0, 240, 255, 0.12),
    inset 0 0 50px rgba(0, 240, 255, 0.03);
}

.employee-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, #00f0ff 35%, #a855f7 70%, transparent);
  opacity: 0.8;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(0, 240, 255, 0.14);
}

.page-heading {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: #eafeff;
  text-shadow: 0 0 14px rgba(0, 240, 255, 0.45);
}

.page-subtitle {
  margin: 0;
  font-family: var(--font-display);
  font-size: 10px;
  letter-spacing: 0.32em;
  color: #3fd8e8;
  text-transform: uppercase;
}

.page-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-top: 4px;
}

.online-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 4px 10px;
  font-family: var(--font-display);
  font-size: 10px;
  letter-spacing: 0.18em;
  color: #4be3b5;
  border: 1px solid rgba(34, 230, 165, 0.35);
  background: rgba(34, 230, 165, 0.06);
  clip-path: polygon(6px 0, 100% 0, 100% calc(100% - 6px), calc(100% - 6px) 100%, 0 100%, 0 6px);
}

.online-badge .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22e6a5;
  box-shadow: 0 0 8px #22e6a5;
  animation: badge-pulse 1.8s ease-in-out infinite;
}

@keyframes badge-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.2;
  }
}

.search-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 2px 14px;
  padding: 14px 16px;
  margin-bottom: 14px;
  background: rgba(0, 240, 255, 0.03);
  border: 1px solid rgba(0, 240, 255, 0.12);
  border-radius: 4px;
}

.search-bar :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 0;
}

.toolbar {
  margin-bottom: 14px;
}

.pagination {
  margin-top: 18px;
  justify-content: flex-end;
}
</style>
