<template>
  <div class="canvas-editor">
    <!-- 顶部导航 -->
    <header class="editor-header">
      <div class="header-left">
        <el-button text @click="goBack">← 返回</el-button>
        <span class="project-name">{{ projectName }}</span>
      </div>
      <div class="header-right">
        <el-switch
          v-model="aiPanelVisible"
          active-text="AI"
          size="small"
          style="--el-switch-on-color: #764ba2"
        />
        <el-button size="small" @click="saveAll">保存</el-button>
      </div>
    </header>

    <div class="editor-body">
      <!-- 左侧: 节点面板 -->
      <NodePalette @add-node="handleAddNode" />

      <!-- 中间: 画布 -->
      <div class="canvas-area" ref="canvasRef">
        <FreeCanvas
          :nodes="canvasStore.flowNodes"
          :edges="canvasStore.flowEdges"
          @node-click="handleNodeClick"
          @pane-click="handlePaneClick"
          @node-drag-stop="handleNodeDragStop"
          @connect="handleConnect"
          @edge-click="handleEdgeClick"
        />
      </div>

      <!-- 右侧: 属性/AI面板 -->
      <div class="right-panels" v-if="selectedNode || aiPanelVisible">
        <PropertyPanel
          v-if="selectedNode"
          :node="selectedNode"
          @update="handleNodeUpdate"
          @delete="handleNodeDelete"
        />
        <AIPanel
          v-if="aiPanelVisible"
          :project-id="projectId"
          :selected-node-id="selectedNode?.id"
          @result="handleAIResult"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCanvasStore } from '@/stores/canvas'
import { useProjectStore } from '@/stores/project'
import { NodeType } from '@/types'
import type { CanvasNode } from '@/types'
import { ElMessage } from 'element-plus'

import FreeCanvas from '@/components/canvas/FreeCanvas.vue'
import NodePalette from '@/components/panels/NodePalette.vue'
import PropertyPanel from '@/components/panels/PropertyPanel.vue'
import AIPanel from '@/components/panels/AIPanel.vue'

const route = useRoute()
const router = useRouter()
const canvasStore = useCanvasStore()
const projectStore = useProjectStore()

const projectId = computed(() => Number(route.params.projectId))
const projectName = ref('')
const selectedNode = ref<any>(null)
const aiPanelVisible = ref(false)
const canvasRef = ref<HTMLElement | null>(null)

let ws: WebSocket | null = null

// ==================== 生命周期 ====================

onMounted(async () => {
  try {
    const project = await projectStore.fetchProject(projectId.value)
    projectName.value = project.name
    await canvasStore.loadAll(projectId.value)
    connectWebSocket()
  } catch (e: any) {
    ElMessage.error('加载项目失败')
    router.push('/')
  }
})

onUnmounted(() => {
  ws?.close()
})

// ==================== WebSocket ====================

const connectWebSocket = () => {
  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
  ws = new WebSocket(`${protocol}//${location.host}/ws/canvas/${projectId.value}`)
  ws.onopen = () => { canvasStore.wsConnected = true }
  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      canvasStore.handleWsMessage(msg)
    } catch {}
  }
  ws.onclose = () => { canvasStore.wsConnected = false }
}

// ==================== 画布事件 ====================

const handleNodeClick = (nodeId: string) => {
  canvasStore.selectNode(Number(nodeId))
  const canvasNode = canvasStore.nodes.find(n => n.id === Number(nodeId))
  selectedNode.value = canvasNode || null
}

const handlePaneClick = () => {
  canvasStore.selectNode(null)
  selectedNode.value = null
}

const handleNodeDragStop = (nodeId: string, position: { x: number; y: number }) => {
  canvasStore.updateNodePosition(nodeId, position.x, position.y)
}

const handleConnect = async (connection: { source: string; target: string; sourceHandle?: string; targetHandle?: string }) => {
  try {
    await canvasStore.addConnection(connection)
  } catch (e: any) {
    ElMessage.error('创建连接失败')
  }
}

const handleEdgeClick = (edgeId: string) => {
  // 点击连接线可删除
  canvasStore.removeConnection(edgeId)
}

// ==================== 节点操作 ====================

const handleAddNode = async (nodeType: NodeType) => {
  // 默认在画布中心创建
  const x = 300 + Math.random() * 200
  const y = 200 + Math.random() * 200
  try {
    await canvasStore.addNode(projectId.value, nodeType, x, y)
  } catch (e: any) {
    ElMessage.error('创建节点失败')
  }
}

const handleNodeUpdate = async (data: any) => {
  if (!selectedNode.value?.id) return
  try {
    await canvasStore.updateNodeData(selectedNode.value.id, data)
    // 刷新显示
    const updated = canvasStore.nodes.find(n => n.id === selectedNode.value.id)
    selectedNode.value = updated || null
  } catch {
    ElMessage.error('更新失败')
  }
}

const handleNodeDelete = async () => {
  if (!selectedNode.value?.id) return
  try {
    await canvasStore.removeNode(String(selectedNode.value.id))
    selectedNode.value = null
  } catch {
    ElMessage.error('删除失败')
  }
}

const handleAIResult = (result: { nodeId?: number; url: string }) => {
  // AI 生成完成后的处理
}

// ==================== 其他 ====================

const saveAll = () => {
  ElMessage.success('所有更改已自动保存')
}

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.canvas-editor {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #0f0f1a;
  color: #e0e0e0;
}

.editor-header {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #1a1a2e;
  border-bottom: 1px solid #2a2a4a;
  flex-shrink: 0;
}
.header-left { display: flex; align-items: center; gap: 12px; }
.project-name { font-size: 1rem; font-weight: 600; }
.header-right { display: flex; align-items: center; gap: 12px; }

.editor-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.canvas-area {
  flex: 1;
  position: relative;
}

.right-panels {
  width: 340px;
  background: #1a1a2e;
  border-left: 1px solid #2a2a4a;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
</style>
