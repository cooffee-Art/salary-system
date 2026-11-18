<script setup>
// 全局根组件：粒子背景常驻最底层，路由出口在其上方
import ParticleBackground from './components/ParticleBackground.vue'
</script>

<template>
  <ParticleBackground />
  <router-view />
</template>
