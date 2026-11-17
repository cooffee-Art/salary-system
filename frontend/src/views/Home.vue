<template>
  <div class="home-page">
    <el-card class="home-card" shadow="always">
      <el-result icon="success" title="登录成功" sub-title="阶段一已完成，员工管理页面将在阶段二实现">
        <template #extra>
          <p class="token-line">
            当前 token：<code>{{ token }}</code>
          </p>
          <el-button type="primary" @click="handleLogout">退出登录</el-button>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const token = ref(localStorage.getItem('token') || '')

function handleLogout() {
  localStorage.removeItem('token')
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.home-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 24px;
}

.home-card {
  width: 560px;
  max-width: 100%;
}

.token-line {
  margin: 0 0 16px;
  font-size: 13px;
  color: #606266;
}

.token-line code {
  padding: 2px 6px;
  border-radius: 4px;
  background: #f4f4f5;
  color: #e6a23c;
}
</style>
