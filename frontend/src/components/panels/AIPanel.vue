<template>
  <div class="ai-panel">
    <div class="panel-header">
      <span>🤖 AI 生成</span>
    </div>

    <div class="panel-body">
      <!-- 提示词输入 -->
      <div class="prop-group">
        <label>生成提示词</label>
        <el-input
          v-model="prompt"
          type="textarea"
          :rows="4"
          placeholder="描述你想要生成的图片或视频内容..."
        />
      </div>

      <div class="prop-group">
        <label>负向提示词（可选）</label>
        <el-input v-model="negativePrompt" size="small" placeholder="不希望出现的内容..." />
      </div>

      <!-- 生成选项 -->
      <div class="option-row">
        <span class="option-label">尺寸</span>
        <el-select v-model="sizePreset" size="small" @change="onSizeChange">
          <el-option label="1024×1024" value="1024x1024" />
          <el-option label="1920×1080" value="1920x1080" />
          <el-option label="1280×720" value="1280x720" />
          <el-option label="720×1280" value="720x1280" />
        </el-select>
      </div>

      <!-- 生成按钮 -->
      <div class="action-buttons">
        <el-button
          type="primary"
          @click="generateImage"
          :loading="generating === 'image'"
          class="action-btn image-btn"
        >
          🖼️ 生成图片
        </el-button>
        <el-button
          type="success"
          @click="generateVideo"
          :loading="generating === 'video'"
          class="action-btn video-btn"
        >
          🎬 生成视频
        </el-button>
      </div>

      <!-- 最近的生成结果 -->
      <div class="recent-section" v-if="recentTasks.length > 0">
        <div class="section-title">最近生成</div>
        <div v-for="task in recentTasks" :key="task.id" class="task-item">
          <div class="task-header">
            <el-tag :type="task.status === 'COMPLETED' ? 'success' : task.status === 'FAILED' ? 'danger' : 'warning'" size="small">
              {{ task.taskType === 'IMAGE' ? '🖼️' : '🎬' }} {{ statusText(task.status) }}
            </el-tag>
          </div>
          <div class="task-prompt">{{ task.prompt }}</div>
          <img v-if="task.taskType === 'IMAGE' && task.resultUrl" :src="task.resultUrl" class="task-result" />
          <video v-if="task.taskType === 'VIDEO' && task.resultUrl" :src="task.resultUrl" controls class="task-result" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { aiApi } from '@/api/ai'
import type { AiTask } from '@/types'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  projectId: number
  selectedNodeId?: number
}>()

const emit = defineEmits<{
  result: [result: { nodeId?: number; url: string; type: string }]
}>()

const prompt = ref('')
const negativePrompt = ref('')
const sizePreset = ref('1024x1024')
const generating = ref<string | null>(null)
const recentTasks = ref<AiTask[]>([])

const onSizeChange = () => {}

const generateImage = async () => {
  if (!prompt.value.trim()) {
    ElMessage.warning('请输入提示词')
    return
  }
  generating.value = 'image'
  try {
    const [w, h] = sizePreset.value.split('x').map(Number)
    const res = await aiApi.generateImage({
      prompt: prompt.value,
      projectId: props.projectId,
      nodeId: props.selectedNodeId,
      negativePrompt: negativePrompt.value,
      width: w,
      height: h
    })
    recentTasks.value.unshift(res.data)
    if (res.data.resultUrl) {
      emit('result', { nodeId: props.selectedNodeId, url: res.data.resultUrl, type: 'image' })
    }
    ElMessage.success('图片生成任务已提交')
  } catch (e: any) {
    ElMessage.error('生成失败: ' + (e.response?.data?.error || e.message))
  } finally {
    generating.value = null
  }
}

const generateVideo = async () => {
  if (!prompt.value.trim()) {
    ElMessage.warning('请输入提示词')
    return
  }
  generating.value = 'video'
  try {
    const [w, h] = sizePreset.value.split('x').map(Number)
    const res = await aiApi.generateVideo({
      prompt: prompt.value,
      projectId: props.projectId,
      nodeId: props.selectedNodeId,
      negativePrompt: negativePrompt.value,
      width: w,
      height: h,
      duration: 5
    })
    recentTasks.value.unshift(res.data)
    if (res.data.resultUrl) {
      emit('result', { nodeId: props.selectedNodeId, url: res.data.resultUrl, type: 'video' })
    }
    ElMessage.success('视频生成任务已提交')
  } catch (e: any) {
    ElMessage.error('生成失败: ' + (e.response?.data?.error || e.message))
  } finally {
    generating.value = null
  }
}

const statusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '排队中', PROCESSING: '生成中', COMPLETED: '完成', FAILED: '失败'
  }
  return map[status] || status
}
</script>

<style scoped>
.ai-panel { display: flex; flex-direction: column; border-top: 1px solid #2a2a4a; }
.panel-header {
  padding: 12px 16px; font-size: 13px; font-weight: 600;
  color: #888; border-bottom: 1px solid #2a2a4a;
}
.panel-body {
  flex: 1; overflow-y: auto; padding: 12px 16px;
  display: flex; flex-direction: column; gap: 12px;
}
.prop-group { display: flex; flex-direction: column; gap: 4px; }
.prop-group label { font-size: 11px; color: #666; }
.option-row { display: flex; align-items: center; justify-content: space-between; }
.option-label { font-size: 11px; color: #666; }
.action-buttons { display: flex; flex-direction: column; gap: 8px; }
.action-btn { width: 100%; }
.image-btn { background: linear-gradient(135deg, #667eea, #764ba2); border: none; }
.video-btn { background: linear-gradient(135deg, #11998e, #38ef7d); border: none; }
.recent-section { margin-top: 12px; }
.section-title { font-size: 12px; color: #666; margin-bottom: 8px; }
.task-item { background: rgba(255,255,255,0.03); border-radius: 8px; padding: 8px; margin-bottom: 8px; }
.task-header { margin-bottom: 4px; }
.task-prompt { font-size: 11px; color: #aaa; margin-bottom: 4px; }
.task-result { width: 100%; border-radius: 6px; max-height: 150px; object-fit: cover; }
</style>
