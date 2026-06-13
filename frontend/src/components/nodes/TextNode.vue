<template>
  <div class="text-node">
    <Handle type="target" :position="Position.Left" id="in-left" />
    <Handle type="target" :position="Position.Top" id="in-top" />
    <Handle type="source" :position="Position.Right" id="out-right" />
    <div class="node-header">
      <span class="node-icon">💬</span>
      <span class="node-label">{{ data.label || '台词' }}</span>
    </div>
    <div class="node-body">
      <div class="text-preview">
        <div class="speaker-line" v-if="data.speaker">
          <span class="speaker-tag">{{ data.speaker }}</span>
          <span class="emotion-tag" v-if="data.emotion">{{ data.emotion }}</span>
        </div>
        <div class="content-text" :style="{ fontSize: (data.fontSize || 14) + 'px' }">
          {{ data.content || '请输入台词...' }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'

defineProps<{
  data: {
    label: string
    title?: string
    content?: string
    speaker?: string
    emotion?: string
    fontSize?: number
  }
}>()
</script>

<style scoped>
.text-node {
  min-width: 200px; max-width: 250px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border: 2px solid #ffd369;
  border-radius: 12px;
  font-size: 12px;
  color: #e0e0e0;
  box-shadow: 0 4px 20px rgba(255, 211, 105, 0.1);
}
.node-header {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 12px;
  background: rgba(255, 211, 105, 0.1);
  border-radius: 10px 10px 0 0;
  font-weight: 600;
}
.node-icon { font-size: 16px; }
.node-body { padding: 10px 12px; }
.speaker-line { display: flex; gap: 6px; margin-bottom: 8px; }
.speaker-tag {
  padding: 1px 8px; border-radius: 10px;
  background: rgba(102, 126, 234, 0.2);
  color: #667eea; font-size: 11px;
}
.emotion-tag {
  padding: 1px 8px; border-radius: 10px;
  background: rgba(255, 211, 105, 0.15);
  color: #ffd369; font-size: 11px;
}
.content-text {
  background: rgba(0,0,0,0.2);
  padding: 8px 10px;
  border-radius: 8px;
  border-left: 3px solid #ffd369;
  line-height: 1.5;
  color: #ccc;
  max-height: 80px;
  overflow-y: auto;
  white-space: pre-wrap;
}
</style>
