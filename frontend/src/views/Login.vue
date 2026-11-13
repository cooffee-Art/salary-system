<template>
  <div class="login-page">
    <el-card class="login-card" shadow="always">
      <template #header>
        <h1 class="login-title">员工管理系统</h1>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            clearable
            autocomplete="username"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            autocomplete="current-password"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            class="login-button"
            type="primary"
            :loading="loading"
            native-type="submit"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <p class="login-tip">当前为 Mock 环境，任意用户名 / 密码均可登录</p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import request from '../api/request'

const router = useRouter()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

/**
 * POST /api/auth/login
 * request 的 baseURL 已是 '/api'，这里只需写 '/auth/login'。
 *
 * 注意：响应拦截器返回的是完整的 { code, data, message }，
 * 所以 token 位于 res.data.token（而不是 res.token）。
 */
async function handleLogin() {
  if (loading.value) return

  try {
    await formRef.value.validate()
  } catch {
    // 表单校验未通过，Element Plus 已给出提示，直接返回
    return
  }

  loading.value = true
  try {
    const res = await request.post('/auth/login', {
      username: form.username,
      password: form.password,
    })

    const token = res?.data?.token
    if (!token) {
      ElMessage.error('登录响应异常：未返回 token')
      return
    }

    localStorage.setItem('token', token)
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    // 拦截器里已经 ElMessage.error 提示过了，这里不再重复弹窗
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 24px;
  background: linear-gradient(135deg, #409eff 0%, #79bbff 45%, #d9ecff 100%);
}

.login-card {
  width: 400px;
  max-width: 100%;
  border-radius: 8px;
}

.login-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  text-align: center;
  color: #303133;
  letter-spacing: 1px;
}

.login-button {
  width: 100%;
}

.login-tip {
  margin: 0;
  font-size: 12px;
  line-height: 1.6;
  text-align: center;
  color: #909399;
}
</style>
