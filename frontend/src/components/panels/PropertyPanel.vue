<template>
  <div class="property-panel">
    <div class="panel-header">
      <span>{{ panelTitle }}</span>
      <el-button size="small" type="danger" text @click="$emit('delete')">删除</el-button>
    </div>

    <div class="panel-body" v-if="nodeData">
      <!-- 通用属性 -->
      <div class="prop-group">
        <label>标签</label>
        <el-input v-model="localData.label" size="small" @change="emitUpdate" />
      </div>

      <!-- 角色节点 -->
      <template v-if="nodeData.nodeType === 'CHARACTER'">
        <div class="prop-group"><label>姓名</label><el-input v-model="localData.name" size="small" @change="emitUpdate" /></div>
        <div class="prop-group"><label>年龄</label><el-input-number v-model="localData.age" :min="1" :max="120" size="small" @change="emitUpdate" /></div>
        <div class="prop-group">
          <label>性别</label>
          <el-select v-model="localData.gender" size="small" @change="emitUpdate">
            <el-option label="男" value="男" /><el-option label="女" value="女" /><el-option label="其他" value="其他" />
          </el-select>
        </div>
        <div class="prop-group"><label>性格</label><el-input v-model="localData.personality" size="small" placeholder="勇敢、善良、机智..." @change="emitUpdate" /></div>
        <div class="prop-group"><label>声线</label><el-input v-model="localData.voiceTone" size="small" placeholder="沉稳、清脆..." @change="emitUpdate" /></div>
        <div class="prop-group"><label>描述</label><el-input v-model="localData.description" type="textarea" :rows="3" size="small" @change="emitUpdate" /></div>
      </template>

      <!-- 场景节点 -->
      <template v-if="nodeData.nodeType === 'SCENE'">
        <div class="prop-group"><label>名称</label><el-input v-model="localData.name" size="small" @change="emitUpdate" /></div>
        <div class="prop-group"><label>地点</label><el-input v-model="localData.location" size="small" placeholder="城市、建筑、自然..." @change="emitUpdate" /></div>
        <div class="prop-group">
          <label>时间</label>
          <el-select v-model="localData.timeOfDay" size="small" @change="emitUpdate">
            <el-option label="清晨" value="清晨" /><el-option label="白天" value="白天" />
            <el-option label="黄昏" value="黄昏" /><el-option label="夜晚" value="夜晚" />
          </el-select>
        </div>
        <div class="prop-group">
          <label>天气</label>
          <el-select v-model="localData.weather" size="small" @change="emitUpdate">
            <el-option label="晴" value="晴" /><el-option label="多云" value="多云" />
            <el-option label="雨" value="雨" /><el-option label="雪" value="雪" />
          </el-select>
        </div>
        <div class="prop-group"><label>氛围</label><el-input v-model="localData.atmosphere" size="small" placeholder="温馨、紧张、神秘..." @change="emitUpdate" /></div>
        <div class="prop-group"><label>描述</label><el-input v-model="localData.description" type="textarea" :rows="3" size="small" @change="emitUpdate" /></div>
      </template>

      <!-- 台词节点 -->
      <template v-if="nodeData.nodeType === 'TEXT'">
        <div class="prop-group"><label>标题</label><el-input v-model="localData.title" size="small" @change="emitUpdate" /></div>
        <div class="prop-group"><label>说话人</label><el-input v-model="localData.speaker" size="small" @change="emitUpdate" /></div>
        <div class="prop-group">
          <label>情绪</label>
          <el-select v-model="localData.emotion" size="small" @change="emitUpdate">
            <el-option label="平静" value="平静" /><el-option label="愤怒" value="愤怒" />
            <el-option label="悲伤" value="悲伤" /><el-option label="喜悦" value="喜悦" />
            <el-option label="恐惧" value="恐惧" /><el-option label="惊讶" value="惊讶" />
          </el-select>
        </div>
        <div class="prop-group"><label>字号</label><el-input-number v-model="localData.fontSize" :min="10" :max="32" size="small" @change="emitUpdate" /></div>
        <div class="prop-group"><label>台词内容</label><el-input v-model="localData.content" type="textarea" :rows="4" size="small" @change="emitUpdate" /></div>
      </template>

      <!-- 视频节点 -->
      <template v-if="nodeData.nodeType === 'VIDEO'">
        <div class="prop-group"><label>标题</label><el-input v-model="localData.title" size="small" @change="emitUpdate" /></div>
        <div class="prop-group"><label>提示词</label><el-input v-model="localData.prompt" type="textarea" :rows="3" size="small" placeholder="描述要生成的视频内容..." @change="emitUpdate" /></div>
        <div class="prop-group"><label>时长(秒)</label><el-input-number v-model="localData.duration" :min="3" :max="30" size="small" @change="emitUpdate" /></div>
        <div class="prop-group">
          <label>状态</label>
          <el-tag :type="statusTagType" size="small">{{ statusLabel }}</el-tag>
        </div>
        <div class="prop-group" v-if="localData.videoUrl">
          <video :src="localData.videoUrl" controls style="width:100%; border-radius: 8px; max-height: 180px" />
        </div>
        <div class="prop-group" v-if="localData.errorMsg">
          <el-alert :title="localData.errorMsg" type="error" :closable="false" />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { CanvasNode } from '@/types'

const props = defineProps<{
  node: CanvasNode
}>()

const emit = defineEmits<{
  update: [data: any]
  delete: []
}>()

const parseNodeData = (node: CanvasNode) => {
  try {
    const parsed = node.nodeData ? JSON.parse(node.nodeData) : {}
    return { label: node.label, nodeType: node.nodeType, ...parsed }
  } catch {
    return { label: node.label, nodeType: node.nodeType }
  }
}

const nodeData = ref<any>(parseNodeData(props.node))
const localData = ref<any>({ ...nodeData.value })

watch(() => props.node, () => {
  nodeData.value = parseNodeData(props.node)
  localData.value = { ...nodeData.value }
})

const panelTitle = computed(() => {
  const map: Record<string, string> = {
    CHARACTER: '角色属性', SCENE: '场景属性', TEXT: '台词属性', VIDEO: '视频属性'
  }
  return map[nodeData.value.nodeType] || '属性'
})

const statusLabel = computed(() => {
  const map: Record<string, string> = {
    PENDING: '待生成', PROCESSING: '生成中', COMPLETED: '已完成', FAILED: '失败'
  }
  return map[localData.value.status] || '待生成'
})

const statusTagType = computed(() => {
  const map: Record<string, string> = {
    PENDING: 'info', PROCESSING: 'warning', COMPLETED: 'success', FAILED: 'danger'
  }
  return map[localData.value.status] || 'info'
})

const emitUpdate = () => {
  emit('update', localData.value)
}
</script>

<style scoped>
.property-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.panel-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px;
  font-size: 13px; font-weight: 600;
  color: #888;
  border-bottom: 1px solid #2a2a4a;
}
.panel-body {
  flex: 1; overflow-y: auto; padding: 12px 16px;
  display: flex; flex-direction: column; gap: 12px;
}
.prop-group {
  display: flex; flex-direction: column; gap: 4px;
}
.prop-group label {
  font-size: 11px; color: #666; text-transform: uppercase; letter-spacing: .5px;
}
</style>
