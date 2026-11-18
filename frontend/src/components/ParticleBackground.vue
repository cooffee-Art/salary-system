<template>
  <canvas ref="canvasRef" class="particle-canvas" aria-hidden="true"></canvas>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'

/**
 * 全局动态粒子连线背景
 *
 * 只做渲染，不含任何业务逻辑：
 * - 全屏 fixed，z-index: -1，位于 #app 层叠上下文中所有内容之下
 * - pointer-events: none，绝不拦截点击/输入
 * - 卸载时取消 rAF 并移除全部监听
 */

const canvasRef = ref(null)

const PARTICLE_COLORS = [
  { rgb: '0, 240, 255', solid: '#00f0ff' }, // 青色
  { rgb: '138, 43, 226', solid: '#8a2be2' }, // 紫色
]

const LINK_DISTANCE = 130 // 粒子间连线距离
const MOUSE_DISTANCE = 180 // 鼠标吸附距离
const MAX_DPR = 2
const BG_ALPHA = 0.3 // 拖尾强度

let ctx = null
let rafId = 0
let particles = []
let width = 0
let height = 0
let dpr = 1
let bgGradient = null

const pointer = { x: -9999, y: -9999 }

function buildParticles() {
  const count = Math.min(120, Math.max(60, Math.round((width * height) / 15000)))

  particles = Array.from({ length: count }, () => {
    const color = PARTICLE_COLORS[Math.random() < 0.62 ? 0 : 1]
    return {
      x: Math.random() * width,
      y: Math.random() * height,
      vx: (Math.random() - 0.5) * 0.4,
      vy: (Math.random() - 0.5) * 0.4,
      r: Math.random() * 1.3 + 0.8,
      rgb: color.rgb,
      solid: color.solid,
      phase: Math.random() * Math.PI * 2,
      twinkle: 0.5 + Math.random() * 0.8,
    }
  })
}

function resizeCanvas() {
  const canvas = canvasRef.value
  if (!canvas) return

  width = window.innerWidth
  height = window.innerHeight
  dpr = Math.min(window.devicePixelRatio || 1, MAX_DPR)

  canvas.width = Math.floor(width * dpr)
  canvas.height = Math.floor(height * dpr)
  canvas.style.width = `${width}px`
  canvas.style.height = `${height}px`

  ctx = canvas.getContext('2d')
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)

  bgGradient = ctx.createRadialGradient(
    width * 0.5,
    height * 0.35,
    0,
    width * 0.5,
    height * 0.35,
    Math.max(width, height) * 0.75,
  )
  bgGradient.addColorStop(0, '#0a1626')
  bgGradient.addColorStop(1, '#050811')

  buildParticles()
}

function renderFrame() {
  rafId = requestAnimationFrame(renderFrame)
  if (!ctx) return

  const now = performance.now() / 1000

  // 半透明覆盖：清屏 + 轻微拖尾
  ctx.globalAlpha = BG_ALPHA
  ctx.fillStyle = bgGradient
  ctx.fillRect(0, 0, width, height)
  ctx.globalAlpha = 1

  // 粒子
  for (const p of particles) {
    p.x += p.vx
    p.y += p.vy

    if (p.x < -20) p.x = width + 20
    else if (p.x > width + 20) p.x = -20
    if (p.y < -20) p.y = height + 20
    else if (p.y > height + 20) p.y = -20

    const radius = Math.max(0.5, p.r + Math.sin(now * p.twinkle + p.phase) * 0.6)

    ctx.beginPath()
    ctx.arc(p.x, p.y, radius, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(${p.rgb}, 0.9)`
    ctx.shadowBlur = 12
    ctx.shadowColor = p.solid
    ctx.fill()
  }
  ctx.shadowBlur = 0

  // 粒子间连线（平方距离比较，避免大量 Math.hypot）
  const linkDistanceSq = LINK_DISTANCE * LINK_DISTANCE

  for (let i = 0; i < particles.length; i += 1) {
    const a = particles[i]

    for (let j = i + 1; j < particles.length; j += 1) {
      const b = particles[j]
      const dx = a.x - b.x
      const dy = a.y - b.y
      const distSq = dx * dx + dy * dy

      if (distSq < linkDistanceSq) {
        const alpha = (1 - Math.sqrt(distSq) / LINK_DISTANCE) * 0.4
        ctx.globalAlpha = alpha
        ctx.strokeStyle = `rgb(${a.rgb})`
        ctx.lineWidth = 0.7
        ctx.beginPath()
        ctx.moveTo(a.x, a.y)
        ctx.lineTo(b.x, b.y)
        ctx.stroke()
      }
    }
  }
  ctx.globalAlpha = 1

  // 鼠标交互
  if (pointer.x > -1000) {
    const mouseDistanceSq = MOUSE_DISTANCE * MOUSE_DISTANCE

    for (const p of particles) {
      const dx = p.x - pointer.x
      const dy = p.y - pointer.y
      const distSq = dx * dx + dy * dy

      if (distSq < mouseDistanceSq) {
        const alpha = (1 - Math.sqrt(distSq) / MOUSE_DISTANCE) * 0.6
        ctx.globalAlpha = alpha
        ctx.strokeStyle = 'rgb(0, 240, 255)'
        ctx.lineWidth = 1
        ctx.beginPath()
        ctx.moveTo(p.x, p.y)
        ctx.lineTo(pointer.x, pointer.y)
        ctx.stroke()
      }
    }
    ctx.globalAlpha = 1

    const glow = ctx.createRadialGradient(
      pointer.x,
      pointer.y,
      0,
      pointer.x,
      pointer.y,
      70,
    )
    glow.addColorStop(0, 'rgba(0, 240, 255, 0.22)')
    glow.addColorStop(1, 'rgba(0, 240, 255, 0)')

    ctx.fillStyle = glow
    ctx.beginPath()
    ctx.arc(pointer.x, pointer.y, 70, 0, Math.PI * 2)
    ctx.fill()
  }
}

function onPointerMove(event) {
  pointer.x = event.clientX
  pointer.y = event.clientY
}

function onPointerLeave() {
  pointer.x = -9999
  pointer.y = -9999
}

onMounted(() => {
  resizeCanvas()

  window.addEventListener('resize', resizeCanvas)
  window.addEventListener('mousemove', onPointerMove, { passive: true })
  window.addEventListener('blur', onPointerLeave)
  document.addEventListener('mouseleave', onPointerLeave)

  rafId = requestAnimationFrame(renderFrame)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(rafId)
  rafId = 0

  window.removeEventListener('resize', resizeCanvas)
  window.removeEventListener('mousemove', onPointerMove)
  window.removeEventListener('blur', onPointerLeave)
  document.removeEventListener('mouseleave', onPointerLeave)

  particles = []
  ctx = null
})
</script>

<style scoped>
/*
 * z-index:-1 依赖 #app 的层叠上下文（style.css 中 #app 为 position:relative + z-index:1），
 * 因此 canvas 会绘制在 #app 内容之下、body 背景之上。
 */
.particle-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: -1;
  display: block;
  pointer-events: none;
}
</style>
