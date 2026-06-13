<template>
  <div class="node-palette">
    <div class="palette-title">节点面板</div>
    <div class="palette-items">
      <div
        class="palette-item"
        v-for="item in nodeTypes"
        :key="item.type"
        @click="$emit('add-node', item.type)"
        :draggable="true"
        @dragstart="handleDragStart($event, item.type)"
      >
        <span class="item-icon">{{ item.icon }}</span>
        <div class="item-info">
          <span class="item-name">{{ item.name }}</span>
          <span class="item-desc">{{ item.desc }}</span>
        </div>
      </div>
    </div>
    <div class="palette-hint">
      拖拽或点击添加到画布
    </div>
  </div>
</template>

<script setup lang="ts">
import { NodeType } from '@/types'

defineEmits<{
  'add-node': [nodeType: NodeType]
}>()

const nodeTypes = [
  { type: NodeType.CHARACTER, name: '角色', icon: '👤', desc: '人物角色设定' },
  { type: NodeType.SCENE, name: '场景', icon: '🎬', desc: '场景环境描述' },
  { type: NodeType.TEXT, name: '台词', icon: '💬', desc: '对话与旁白' },
  { type: NodeType.VIDEO, name: '视频', icon: '🎥', desc: 'AI 生成视频' }
]

const handleDragStart = (event: DragEvent, nodeType: NodeType) => {
  event.dataTransfer?.setData('nodeType', nodeType)
}
</script>

<style scoped>
.node-palette {
  width: 180px;
  background: #1a1a2e;
  border-right: 1px solid #2a2a4a;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.palette-title {
  padding: 12px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 1px;
  border-bottom: 1px solid #2a2a4a;
}
.palette-items {
  flex: 1;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.palette-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  border: 1px solid transparent;
}
.palette-item:hover {
  background: rgba(102, 126, 234, 0.1);
  border-color: #667eea;
}
.item-icon { font-size: 22px; }
.item-info { display: flex; flex-direction: column; }
.item-name { font-size: 13px; font-weight: 500; }
.item-desc { font-size: 11px; color: #666; }
.palette-hint {
  padding: 12px;
  font-size: 11px;
  color: #555;
  text-align: center;
  border-top: 1px solid #2a2a4a;
}
</style>
