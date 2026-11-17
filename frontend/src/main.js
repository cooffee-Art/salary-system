import { createApp } from 'vue'

// Element Plus（含中文语言包）
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'

import './style.css'

const app = createApp(App)

// 先挂路由，再挂 Element Plus，保证页面内可直接使用 el-* 组件
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
