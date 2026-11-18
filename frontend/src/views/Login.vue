<template>
  <div class="login-page">
    <el-card class="login-card" shadow="never">
      <template #header>
        <div class="login-brand">
          <span class="brand-mark" aria-hidden="true"></span>
          <h1 class="login-title">员工管理系统</h1>
          <p class="login-subtitle">EMPLOYEE ADMIN SYSTEM</p>
        </div>
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
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>

        <el-form-item>
          <!-- 只保留 submit 一个入口：点击按钮触发 <form> submit，回车也触发 submit，
               避免 click + submit + keyup.enter 三重触发导致重复请求 -->
          <el-button
            class="login-button"
            type="primary"
            :loading="loading"
            native-type="submit"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-status">
        <span class="status-dot" aria-hidden="true"></span>
        <span>SECURE CHANNEL · ONLINE</span>
      </div>
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
 * 响应拦截器已经解包 { code, message, data }，所以 res 就是 data，
 * token 直接取 res.token。
 */
async function handleLogin() {
  // 同步置位，先于任何 await：即使有多个触发源，也只放行一次请求
  if (loading.value) return
  loading.value = true

  try {
    await formRef.value.validate()
  } catch {
    // 表单校验未通过，Element Plus 已给出提示，恢复按钮后返回
    loading.value = false
    return
  }

  try {
    const res = await request.post('/auth/login', {
      username: form.username,
      password: form.password,
    })

    const token = res?.token
    if (!token) {
      ElMessage.error('登录响应异常：未返回 token')
      return
    }

    localStorage.setItem('token', token)
    localStorage.setItem('username', form.username.trim())
    ElMessage.success('登录成功')
    router.push('/employees')
  } catch {
    // 拦截器里已经 ElMessage.error 提示过了，这里不再重复弹窗
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 背景交给全局 ParticleBackground，这里只保留布局与玻璃卡片 */
.login-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 24px;
  background: transparent;
}

/* 玻璃拟态卡片：深色半透明 + 强模糊 + 霓虹描边 */
.login-card {
  position: relative;
  z-index: 2;
  width: 400px;
  max-width: 100%;
  background: rgba(6, 14, 26, 0.62);
  border: 1px solid rgba(0, 240, 255, 0.5);
  border-radius: 0;
  box-shadow:
    inset 0 0 40px rgba(0, 240, 255, 0.05),
    inset 0 1px 0 rgba(191, 252, 255, 0.12);
  backdrop-filter: blur(22px) saturate(1.3);
  -webkit-backdrop-filter: blur(22px) saturate(1.3);
  clip-path: polygon(
    0 0,
    calc(100% - 20px) 0,
    100% 20px,
    100% 100%,
    20px 100%,
    0 calc(100% - 20px)
  );
  filter: drop-shadow(0 0 30px rgba(0, 240, 255, 0.35));
}

/* 顶部霓虹线 */
.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, #00f0ff, transparent);
  opacity: 0.9;
}

.login-card :deep(.el-card__header) {
  padding: 28px 28px 10px;
  border-bottom: none;
  background: radial-gradient(
    ellipse 80% 120% at 50% -30%,
    rgba(0, 240, 255, 0.12),
    transparent 70%
  );
}

.login-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
}

.brand-mark {
  width: 34px;
  height: 34px;
  margin-bottom: 6px;
  border: 1px solid rgba(0, 240, 255, 0.6);
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.25), rgba(43, 108, 255, 0.1));
  box-shadow:
    0 0 14px rgba(0, 240, 255, 0.45),
    inset 0 0 10px rgba(0, 240, 255, 0.25);
  transform: rotate(45deg);
}

.login-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.16em;
  color: #eafeff;
  text-shadow: 0 0 12px rgba(0, 240, 255, 0.55);
}

.login-subtitle {
  margin: 0;
  font-family: var(--font-display);
  font-size: 10px;
  letter-spacing: 0.38em;
  color: #3fd8e8;
  text-transform: uppercase;
}

.login-button {
  width: 100%;
  letter-spacing: 0.28em;
}

.login-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 6px;
  font-family: var(--font-display);
  font-size: 10px;
  letter-spacing: 0.22em;
  color: #4c7f95;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22e6a5;
  box-shadow: 0 0 8px #22e6a5;
  animation: pulse 1.8s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.25;
  }
}

@media (prefers-reduced-motion: reduce) {
  .status-dot {
    animation: none;
  }
}
</style>
