import { createApp } from 'vue'

// Element Plus（含中文语言包 + 深色主题变量）
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 科技风字体（本地打包，不依赖外网 CDN）
import '@fontsource/orbitron/latin-500.css'
import '@fontsource/orbitron/latin-700.css'
import '@fontsource/rajdhani/latin-400.css'
import '@fontsource/rajdhani/latin-600.css'

import App from './App.vue'
import router from './router'

// 全局科技风样式（必须最后引入，才能覆盖 Element Plus 默认样式）
import './style.css'

const app = createApp(App)

app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
