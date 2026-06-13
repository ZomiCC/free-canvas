<template>
  <div class="scene-node">
    <Handle type="source" :position="Position.Right" id="out-right" />
    <Handle type="source" :position="Position.Bottom" id="out-bottom" />
    <Handle type="target" :position="Position.Left" id="in-left" />
    <div class="node-header">
      <span class="node-icon">🎬</span>
      <span class="node-label">{{ data.label || '场景' }}</span>
    </div>
    <div class="node-body">
      <div class="scene-preview">
        <span class="scene-icon">🏞️</span>
      </div>
      <div class="info">
        <div class="info-row"><strong>{{ data.name || '未命名' }}</strong></div>
        <div class="info-row" v-if="data.location">📍 {{ data.location }}</div>
        <div class="info-row" v-if="data.timeOfDay">🕐 {{ data.timeOfDay }}</div>
        <div class="info-row atmosphere" v-if="data.atmosphere">{{ data.atmosphere }}</div>
        <div class="info-row weather" v-if="data.weather">🌤 {{ data.weather }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'

defineProps<{
  data: {
    label: string
    name?: string
    description?: string
    location?: string
    timeOfDay?: string
    atmosphere?: string
    backgroundUrl?: string
    weather?: string
  }
}>()
</script>

<style scoped>
.scene-node {
  min-width: 180px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border: 2px solid #0f3460;
  border-radius: 12px;
  font-size: 12px;
  color: #e0e0e0;
  box-shadow: 0 4px 20px rgba(15, 52, 96, 0.2);
}
.node-header {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 12px;
  background: rgba(15, 52, 96, 0.3);
  border-radius: 10px 10px 0 0;
  font-weight: 600;
}
.node-icon { font-size: 16px; }
.node-body {
  display: flex; gap: 10px; padding: 10px 12px;
}
.scene-preview {
  width: 50px; height: 50px;
  border-radius: 8px;
  background: linear-gradient(135deg, #0f3460, #16213e);
  display: flex; align-items: center; justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
  border: 1px solid #1a1a4a;
}
.info { flex: 1; }
.info-row { margin-bottom: 2px; }
.info-row strong { color: #fff; }
.atmosphere {
  color: #533483;
  font-size: 11px; margin-top: 4px;
  padding: 2px 6px;
  background: rgba(83, 52, 131, 0.1);
  border-radius: 4px;
  display: inline-block;
}
.weather { color: #ffd369; font-size: 11px; }
</style>
