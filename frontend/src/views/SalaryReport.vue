<template>
  <div class="report-page">
    <el-card class="report-card" shadow="never">
      <div class="page-header">
        <div class="page-heading">
          <h2 class="page-title">部门薪资报表</h2>
          <p class="page-subtitle">SYS / DEPARTMENT SALARY REPORT</p>
        </div>
        <span class="online-badge">
          <span class="dot" aria-hidden="true"></span>
          API CONNECTED
        </span>
      </div>

      <div class="controls">
        <el-date-picker
          v-model="month"
          type="month"
          value-format="YYYY-MM"
          placeholder="选择月份"
          style="width: 160px"
          @change="loadReport"
        />
        <el-button type="primary" @click="loadReport">查询</el-button>
        <el-button type="primary" class="generate-btn" :loading="generating" @click="handleGenerate">
          生成该月工资
        </el-button>
      </div>

      <el-table
        v-loading="loading"
        class="report-table"
        :data="reportData"
        border
        stripe
        show-summary
        :summary-method="summary"
        style="width: 100%"
      >
        <el-table-column prop="deptName" label="部门" min-width="140" />
        <el-table-column prop="empCount" label="人数" width="90" align="center" />

        <el-table-column prop="totalBase" label="基本工资合计" min-width="150" align="right">
          <template #default="{ row }">
            <span class="money">{{ money(row.totalBase) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalBonus" label="奖金合计" min-width="140" align="right">
          <template #default="{ row }">
            <span class="money">{{ money(row.totalBonus) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalDeduction" label="扣款合计" min-width="140" align="right">
          <template #default="{ row }">
            <span class="money">{{ money(row.totalDeduction) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalActual" label="实发合计" min-width="150" align="right">
          <template #default="{ row }">
            <span class="money money-strong">{{ money(row.totalActual) }}</span>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="该月暂无薪资数据，可点击「生成该月工资」" :image-size="90" />
        </template>
      </el-table>

      <p class="report-tip">
        报表按部门聚合：实发合计 = 基本工资合计 + 奖金合计 − 扣款合计。
      </p>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { fetchDepartmentReport, generateMonthlySalary } from '@/api/salary'

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

const month = ref(currentMonth())
const reportData = ref([])
const loading = ref(false)
const generating = ref(false)

async function loadReport() {
  if (!month.value) {
    ElMessage.warning('请先选择月份')
    return
  }
  loading.value = true
  try {
    const data = await fetchDepartmentReport(month.value)
    reportData.value = Array.isArray(data) ? data : []
  } catch {
    reportData.value = []
  } finally {
    loading.value = false
  }
}

/** 表格合计行 */
function summary({ columns, data }) {
  return columns.map((column, index) => {
    if (index === 0) return '合计'
    if (column.property === 'empCount') {
      return data.reduce((sum, row) => sum + Number(row.empCount || 0), 0)
    }
    if (
      column.property === 'totalBase' ||
      column.property === 'totalBonus' ||
      column.property === 'totalDeduction' ||
      column.property === 'totalActual'
    ) {
      const total = data.reduce(
        (sum, row) => sum + Number(row[column.property] || 0),
        0,
      )
      return money(total)
    }
    return ''
  })
}

async function handleGenerate() {
  if (!month.value) {
    ElMessage.warning('请先选择月份')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定为 ${month.value} 生成工资吗？已生成过的员工会被跳过。`,
      '生成工资',
      { type: 'warning', confirmButtonText: '确定生成', cancelButtonText: '取消' },
    )
  } catch {
    return
  }

  generating.value = true
  try {
    // raw: true → 拿到 { code, message, data }，便于展示后端的“该月工资已生成”等提示
    const body = await generateMonthlySalary(month.value)
    const result = body?.data || {}
    const generated = result.generated ?? 0
    const skipped = result.skipped ?? 0
    const totalActual = result.totalActual ?? 0
    const message = body?.message || '生成完成'

    ElMessageBox.alert(
      `<div style="line-height:1.9">
         <p style="margin:0 0 6px;color:#00f0ff">${message}</p>
         <p style="margin:0">新增：<b>${generated}</b> 条　跳过：<b>${skipped}</b> 条</p>
         <p style="margin:0">实发合计：<b>¥ ${money(totalActual)}</b></p>
       </div>`,
      '生成结果',
      {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '知道了',
        type: 'success',
      },
    )

    await loadReport()
  } catch {
    // 业务错误（如“该月工资已生成”）已由响应拦截器弹出
  } finally {
    generating.value = false
  }
}

onMounted(loadReport)
</script>

<style scoped>
.report-page {
  max-width: 1320px;
  margin: 0 auto;
  background: transparent;
}

.report-card {
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

.report-card::before {
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
}

.controls {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 14px;
  background: rgba(0, 240, 255, 0.03);
  border: 1px solid rgba(0, 240, 255, 0.12);
  border-radius: 4px;
}

.generate-btn {
  margin-left: auto;
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

.report-tip {
  margin: 14px 0 0;
  font-size: 12px;
  color: #6c8aa5;
  letter-spacing: 0.02em;
}

@media (max-width: 900px) {
  .generate-btn {
    margin-left: 0;
  }
}
</style>
