<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark" aria-hidden="true"></span>
        <div class="brand-text">
          <strong>SALARY SYS</strong>
          <small>EMPLOYEE ADMIN</small>
        </div>
      </div>

      <el-menu :default-active="route.path" router class="side-menu">
        <el-menu-item index="/employees">
          <span class="menu-dot" aria-hidden="true"></span>
          <span>员工管理</span>
        </el-menu-item>
        <el-menu-item index="/salaries">
          <span class="menu-dot" aria-hidden="true"></span>
          <span>薪资管理</span>
        </el-menu-item>
        <el-menu-item index="/salary-report">
          <span class="menu-dot" aria-hidden="true"></span>
          <span>部门报表</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <span class="status-dot" aria-hidden="true"></span>
        API · localhost:8080
      </div>
    </aside>

    <!-- 右侧主体 -->
    <div class="main">
      <header class="topbar">
        <div class="topbar-title">
          <h2>{{ currentTitle }}</h2>
          <p>SALARY MANAGEMENT SYSTEM</p>
        </div>

        <div class="topbar-right">
          <span class="user-chip">{{ username }}</span>
          <el-button size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const username = localStorage.getItem('username') || 'admin'

const currentTitle = computed(() => route.meta.title || '员工管理')

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  ElMessage.success('已退出登录')
  router.replace('/login')
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background: transparent;
}

/* ------------------------------ 侧边栏 ------------------------------ */
.sidebar {
  display: flex;
  flex-direction: column;
  flex: 0 0 220px;
  width: 220px;
  padding: 20px 14px;
  background: rgba(6, 14, 26, 0.55);
  border-right: 1px solid rgba(0, 240, 255, 0.18);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 6px 18px;
  margin-bottom: 14px;
  border-bottom: 1px solid rgba(0, 240, 255, 0.14);
}

.brand-mark {
  width: 26px;
  height: 26px;
  flex: 0 0 26px;
  border: 1px solid rgba(0, 240, 255, 0.6);
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.3), rgba(138, 43, 226, 0.15));
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.45);
  transform: rotate(45deg);
}

.brand-text strong {
  display: block;
  font-family: var(--font-display);
  font-size: 12px;
  letter-spacing: 0.18em;
  color: #eafeff;
}

.brand-text small {
  display: block;
  font-family: var(--font-display);
  font-size: 9px;
  letter-spacing: 0.24em;
  color: #3fd8e8;
}

.sidebar-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: auto;
  padding: 12px 6px 0;
  border-top: 1px solid rgba(0, 240, 255, 0.12);
  font-family: var(--font-display);
  font-size: 9px;
  letter-spacing: 0.16em;
  color: #4c7f95;
}

.sidebar-footer .status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22e6a5;
  box-shadow: 0 0 8px #22e6a5;
}

/* 菜单深色科技风 */
.side-menu {
  border-right: none;
  background: transparent;
}

.side-menu :deep(.el-menu-item) {
  height: 44px;
  margin: 4px 0;
  border: 1px solid transparent;
  border-radius: 3px;
  font-size: 14px;
  letter-spacing: 0.04em;
  color: #9fc4d8;
}

.side-menu :deep(.el-menu-item:hover) {
  background: rgba(0, 240, 255, 0.08);
  border-color: rgba(0, 240, 255, 0.2);
  color: #eafeff;
}

.side-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(0, 240, 255, 0.18), rgba(43, 108, 255, 0.08));
  border-color: rgba(0, 240, 255, 0.45);
  color: #00f0ff;
  box-shadow: inset 0 0 14px rgba(0, 240, 255, 0.18);
}

.menu-dot {
  width: 6px;
  height: 6px;
  margin-right: 10px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 8px currentColor;
}

/* ------------------------------ 顶栏 ------------------------------ */
.main {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: rgba(6, 14, 26, 0.45);
  border-bottom: 1px solid rgba(0, 240, 255, 0.18);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.topbar-title h2 {
  margin: 0;
  font-family: var(--font-display);
  font-size: 16px;
  letter-spacing: 0.14em;
  color: #eafeff;
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.4);
}

.topbar-title p {
  margin: 2px 0 0;
  font-family: var(--font-display);
  font-size: 9px;
  letter-spacing: 0.3em;
  color: #3fd8e8;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px;
  border: 1px solid rgba(0, 240, 255, 0.3);
  background: rgba(0, 240, 255, 0.06);
  font-size: 13px;
  color: #bfefff;
  clip-path: polygon(6px 0, 100% 0, 100% calc(100% - 6px), calc(100% - 6px) 100%, 0 100%, 0 6px);
}

.user-chip::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22e6a5;
  box-shadow: 0 0 8px #22e6a5;
}

.content {
  flex: 1;
  padding: 24px;
  overflow: auto;
}

@media (max-width: 900px) {
  .sidebar {
    flex-basis: 180px;
    width: 180px;
    padding: 16px 10px;
  }

  .content {
    padding: 14px;
  }

  .topbar {
    padding: 0 14px;
  }
}
</style>
