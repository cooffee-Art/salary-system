<template>
  <div class="salary-page">
    <el-card class="salary-card" shadow="never">
      <div class="page-header">
        <div class="page-heading">
          <h2 class="page-title">薪资管理</h2>
          <p class="page-subtitle">SYS / SALARY DATABASE</p>
        </div>
        <span class="online-badge">
          <span class="dot" aria-hidden="true"></span>
          API CONNECTED
        </span>
      </div>

      <!-- 搜索：月份 + 关键字（姓名/工号） -->
      <el-form
        class="search-bar"
        :inline="true"
        :model="searchForm"
        @submit.prevent="handleSearch"
      >
        <el-form-item label="月份">
          <el-date-picker
            v-model="searchForm.month"
            type="month"
            value-format="YYYY-MM"
            placeholder="全部月份"
            clearable
            style="width: 160px"
            @change="handleSearch"
          />
        </el-form-item>
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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="toolbar">
        <el-button type="primary" @click="openCreate">新增薪资</el-button>
      </div>

      <el-table
        v-loading="loading"
        class="salary-table"
        :data="tableData"
        border
        stripe
        row-key="id"
        style="width: 100%"
      >
        <el-table-column prop="empNo" label="工号" width="110" />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="deptName" label="部门" min-width="110" />
        <el-table-column prop="salaryMonth" label="月份" width="100" align="center" />

        <el-table-column label="基本工资" width="130" align="right">
          <template #default="{ row }">
            <span class="money">{{ money(row.basePay) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="奖金" width="120" align="right">
          <template #default="{ row }">
            <span class="money">{{ money(row.bonus) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="扣款" width="120" align="right">
          <template #default="{ row }">
            <span class="money">{{ money(row.deduction) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实发工资" width="140" align="right">
          <template #default="{ row }">
            <span class="money money-strong">{{ money(row.actualPay) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无薪资数据" :image-size="90" />
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

    <!-- 新增 / 编辑薪资 -->
    <el-dialog
      v-model="dialogVisible"
      class="cyber-dialog"
      :title="isEdit ? '编辑薪资' : '新增薪资'"
      width="560px"
      destroy-on-close
      @closed="handleDialogClosed"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="90px">
        <el-form-item label="员工" prop="empId">
          <el-select
            v-model="formData.empId"
            placeholder="请选择员工"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="emp in employees"
              :key="emp.id"
              :label="`${emp.empNo} ${emp.name}`"
              :value="emp.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="月份" prop="salaryMonth">
          <el-date-picker
            v-model="formData.salaryMonth"
            type="month"
            value-format="YYYY-MM"
            placeholder="请选择月份"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="基本工资" prop="basePay">
          <el-input-number
            v-model="formData.basePay"
            :min="0"
            :precision="2"
            :step="100"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="奖金" prop="bonus">
          <el-input-number
            v-model="formData.bonus"
            :min="0"
            :precision="2"
            :step="100"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="扣款" prop="deduction">
          <el-input-number
            v-model="formData.deduction"
            :min="0"
            :precision="2"
            :step="100"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="实发工资">
          <div class="actual-preview">¥ {{ money(actualPay) }}</div>
          <span class="actual-hint">实发 = 基本工资 + 奖金 − 扣款</span>
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
  createSalary,
  deleteSalary,
  fetchSalaryDetail,
  fetchSalaryPage,
  updateSalary,
} from '@/api/salary'
import { fetchAllEmployees } from '@/api/employee'

function currentMonth() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

function money(value) {
  const num = Number(value ?? 0)
  if (!Number.isFinite(num)) return '0.00'
  return num.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

/* ------------------------------ 列表 ------------------------------ */
const loading = ref(false)
const tableData = ref([])
const employees = ref([])

const searchForm = reactive({
  month: currentMonth(),
  keyword: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

async function loadData() {
  loading.value = true
  try {
    const data = await fetchSalaryPage({
      month: searchForm.month || undefined,
      keyword: searchForm.keyword.trim() || undefined,
      current: pagination.page,
      size: pagination.pageSize,
    })
    tableData.value = data?.records ?? []
    pagination.total = Number(data?.total ?? 0)
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

async function loadEmployees() {
  try {
    const data = await fetchAllEmployees()
    employees.value = Array.isArray(data) ? data : []
  } catch {
    employees.value = []
  }
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

function handleReset() {
  searchForm.month = currentMonth()
  searchForm.keyword = ''
  pagination.page = 1
  loadData()
}

/* ------------------------------ 分页 ------------------------------ */
let reloadTimer = null

function scheduleReload() {
  clearTimeout(reloadTimer)
  reloadTimer = setTimeout(loadData, 30)
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

/* ------------------------------ 弹窗 ------------------------------ */
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  empId: null,
  salaryMonth: '',
  basePay: 0,
  bonus: 0,
  deduction: 0,
})

const isEdit = computed(() => formData.id !== null && formData.id !== undefined)

const actualPay = computed(() => {
  const base = Number(formData.basePay) || 0
  const bonus = Number(formData.bonus) || 0
  const deduction = Number(formData.deduction) || 0
  return Math.round((base + bonus - deduction) * 100) / 100
})

const rules = {
  empId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  salaryMonth: [{ required: true, message: '请选择月份', trigger: 'change' }],
  basePay: [{ required: true, message: '请输入基本工资', trigger: 'blur' }],
}

function resetFormData() {
  formData.id = null
  formData.empId = null
  formData.salaryMonth = currentMonth()
  formData.basePay = 0
  formData.bonus = 0
  formData.deduction = 0
}

function openCreate() {
  resetFormData()
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function fillForm(source) {
  formData.id = source.id ?? null
  formData.empId = source.empId ?? source.emp_id ?? source.employeeId ?? null
  formData.salaryMonth = source.salaryMonth ?? source.salary_month ?? ''
  formData.basePay = Number(source.basePay ?? source.base_pay ?? 0)
  formData.bonus = Number(source.bonus ?? 0)
  formData.deduction = Number(source.deduction ?? 0)
}

async function openEdit(row) {
  resetFormData()
  Object.assign(formData, { id: row.id })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())

  try {
    // 用详情接口拿最完整的数据（列表 VO 可能不含 empId）
    const detail = await fetchSalaryDetail(row.id)
    fillForm({ ...row, ...detail })
  } catch {
    fillForm(row)
  }
}

function handleDialogClosed() {
  resetFormData()
}

function buildPayload() {
  return {
    empId: formData.empId,
    salaryMonth: formData.salaryMonth,
    basePay: Number(formData.basePay) || 0,
    bonus: Number(formData.bonus) || 0,
    deduction: Number(formData.deduction) || 0,
    actualPay: actualPay.value,
  }
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateSalary(formData.id, buildPayload())
      ElMessage.success('修改成功')
    } else {
      await createSalary(buildPayload())
      ElMessage.success('新增成功')
      pagination.page = 1
    }
    dialogVisible.value = false
    await loadData()
  } catch {
    // 后端业务错误（如“该员工该月薪资已存在”）由拦截器弹出 message
  } finally {
    submitting.value = false
  }
}

/* ------------------------------ 删除 ------------------------------ */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除 ${row.empName || row.name || ''} ${row.salaryMonth || ''} 的薪资记录吗？`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消' },
    )
  } catch {
    return
  }

  try {
    const result = await deleteSalary(row.id)
    const message = typeof result === 'string' && result.trim() ? result : '删除成功'
    ElMessage.success(message)
    if (tableData.value.length === 1 && pagination.page > 1) {
      pagination.page -= 1
    }
    await loadData()
  } catch {
    // 拦截器已提示
  }
}

onMounted(() => {
  loadEmployees()
  loadData()
})
</script>

<style scoped>
.salary-page {
  max-width: 1320px;
  margin: 0 auto;
  background: transparent;
}

.salary-card {
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

.salary-card::before {
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
  margin: 4px 0 0;
  font-family: var(--font-display);
  font-size: 10px;
  letter-spacing: 0.32em;
  color: #3fd8e8;
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

.money {
  font-family: var(--font-display);
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.02em;
  color: #c9e6f5;
}

.money-strong {
  color: #00f0ff;
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.35);
}

.pagination {
  margin-top: 18px;
  justify-content: flex-end;
}

.actual-preview {
  font-family: var(--font-display);
  font-size: 16px;
  color: #00f0ff;
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.35);
}

.actual-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #6c8aa5;
}
</style>
