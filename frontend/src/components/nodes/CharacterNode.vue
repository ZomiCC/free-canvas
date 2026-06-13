<template>
  <div class="character-node">
    <Handle type="source" :position="Position.Right" id="out-right" />
    <Handle type="source" :position="Position.Bottom" id="out-bottom" />
    <div class="node-header">
      <span class="node-icon">👤</span>
      <span class="node-label">{{ data.label || '角色' }}</span>
    </div>
    <div class="node-body">
      <div class="avatar-circle">{{ (data.name || '?')[0] }}</div>
      <div class="info">
        <div class="info-row"><strong>{{ data.name || '未命名' }}</strong></div>
        <div class="info-row" v-if="data.age">年龄: {{ data.age }}</div>
        <div class="info-row" v-if="data.gender">性别: {{ data.gender }}</div>
        <div class="info-row trait" v-if="data.personality">{{ data.personality }}</div>
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
    age?: number
    gender?: string
    personality?: string
    avatarUrl?: string
    voiceTone?: string
  }
}>()
</script>

<style scoped>
.character-node {
  min-width: 180px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border: 2px solid #e94560;
  border-radius: 12px;
  padding: 0;
  font-size: 12px;
  color: #e0e0e0;
  box-shadow: 0 4px 20px rgba(233, 69, 96, 0.15);
}
.node-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(233, 69, 96, 0.15);
  border-radius: 10px 10px 0 0;
  font-weight: 600;
}
.node-icon { font-size: 16px; }
.node-body {
  display: flex;
  gap: 10px;
  padding: 10px 12px;
}
.avatar-circle {
  width: 40px; height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e94560, #764ba2);
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: bold;
  flex-shrink: 0;
}
.info { flex: 1; }
.info-row { margin-bottom: 2px; }
.info-row strong { color: #fff; }
.trait {
  color: #e94560;
  font-size: 11px;
  margin-top: 4px;
  padding: 2px 6px;
  background: rgba(233, 69, 96, 0.1);
  border-radius: 4px;
  display: inline-block;
}
</style>
