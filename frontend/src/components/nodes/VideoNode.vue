<template>
  <div class="video-node" :class="`status-${(data.status || 'PENDING').toLowerCase()}`">
    <Handle type="target" :position="Position.Left" id="in-left" />
    <Handle type="target" :position="Position.Top" id="in-top" />
    <Handle type="target" :position="Position.Bottom" id="in-bottom" />
    <div class="node-header">
      <span class="node-icon">🎥</span>
      <span class="node-label">{{ data.label || '视频' }}</span>
      <span class="status-badge" :class="`status-${(data.status || 'PENDING').toLowerCase()}`">
        {{ statusText }}
      </span>
    </div>
    <div class="node-body">
      <div class="video-preview" v-if="data.videoUrl">
        <video :src="data.videoUrl" controls class="preview-video" />
      </div>
      <div class="video-preview placeholder" v-else>
        <span class="play-icon">▶</span>
        <span class="placeholder-text">{{ data.status === 'PROCESSING' ? '生成中...' : '待生成' }}</span>
      </div>
      <div class="info">
        <div class="info-row"><strong>{{ data.title || '未命名视频' }}</strong></div>
        <div class="info-row prompt" v-if="data.prompt">{{ data.prompt }}</div>
        <div class="info-row duration" v-if="data.duration">时长: {{ data.duration }}s</div>
      </div>
      <!-- 进度条 -->
      <div class="progress-bar" v-if="data.status === 'PROCESSING'">
        <div class="progress-fill" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Handle, Position } from '@vue-flow/core'

const props = defineProps<{
  data: {
    label: string
    title?: string
    description?: string
    prompt?: string
    videoUrl?: string
    thumbnailUrl?: string
    duration?: number
    status?: string
  }
}>()

const statusText = computed(() => {
  switch (props.data.status) {
    case 'PENDING': return '待生成'
    case 'PROCESSING': return '生成中'
    case 'COMPLETED': return '✓'
    case 'FAILED': return '失败'
    default: return '待生成'
  }
})
</script>

<style scoped>
.video-node {
  min-width: 240px; max-width: 300px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border: 2px solid #667eea;
  border-radius: 12px;
  font-size: 12px;
  color: #e0e0e0;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
  transition: border-color 0.3s;
}
.video-node.status-completed { border-color: #2ecc71; }
.video-node.status-processing { border-color: #f39c12; }
.video-node.status-failed { border-color: #e74c3c; }

.node-header {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 12px;
  background: rgba(102, 126, 234, 0.12);
  border-radius: 10px 10px 0 0;
  font-weight: 600;
}
.node-icon { font-size: 16px; }
.status-badge {
  margin-left: auto;
  padding: 1px 6px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: 600;
}
.status-badge.status-pending { background: rgba(102,126,234,0.2); color: #667eea; }
.status-badge.status-processing { background: rgba(243,156,18,0.2); color: #f39c12; }
.status-badge.status-completed { background: rgba(46,204,113,0.2); color: #2ecc71; }
.status-badge.status-failed { background: rgba(231,76,60,0.2); color: #e74c3c; }

.node-body { padding: 10px 12px; }
.video-preview {
  width: 100%; height: 120px;
  border-radius: 8px;
  background: rgba(0,0,0,0.3);
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 8px;
  overflow: hidden;
}
.video-preview.placeholder { flex-direction: column; gap: 8px; border: 1px dashed #333; }
.play-icon { font-size: 24px; color: #667eea; opacity: 0.6; }
.placeholder-text { font-size: 11px; color: #666; }
.preview-video { width: 100%; height: 100%; object-fit: cover; }

.info { margin-bottom: 4px; }
.info-row { margin-bottom: 2px; }
.info-row strong { color: #fff; }
.prompt {
  color: #888; font-size: 10px;
  overflow: hidden; text-overflow: ellipsis;
  white-space: nowrap; max-width: 200px;
}
.duration { color: #533483; font-size: 11px; }

.progress-bar {
  width: 100%; height: 3px;
  background: rgba(255,255,255,0.1);
  border-radius: 2px;
  overflow: hidden;
  margin-top: 4px;
}
.progress-fill {
  width: 60%;
  height: 100%;
  background: linear-gradient(90deg, #f39c12, #667eea, #764ba2);
  border-radius: 2px;
  animation: progress-ani 1.5s ease-in-out infinite;
}
@keyframes progress-ani {
  0%, 100% { width: 20%; }
  50% { width: 80%; }
}
</style>
