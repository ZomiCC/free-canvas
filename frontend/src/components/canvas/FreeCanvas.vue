<template>
  <div class="free-canvas-wrapper">
    <VueFlow
      v-model:nodes="localNodes"
      v-model:edges="localEdges"
      :node-types="nodeTypes"
      :default-edge-options="{ type: 'smoothstep', animated: true, style: { stroke: '#667eea', strokeWidth: 2 } }"
      :connection-line-style="{ stroke: '#764ba2', strokeWidth: 2 }"
      :snap-to-grid="true"
      :snap-grid="[20, 20]"
      fit-view-on-init
      @node-click="onNodeClick"
      @pane-click="onPaneClick"
      @node-drag-stop="onNodeDragStop"
      @connect="onConnect"
      @edge-click="onEdgeClick"
    >
      <Background :gap="20" :size="1" pattern-color="#2a2a4a" />
      <Controls position="bottom-right" />
      <MiniMap
        position="bottom-left"
        :pannable="true"
        :zoomable="true"
        :node-stroke-color="'#667eea'"
      />
    </VueFlow>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { VueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'
import type { FlowNode, FlowEdge } from '@/types'

import CharacterNode from '@/components/nodes/CharacterNode.vue'
import SceneNode from '@/components/nodes/SceneNode.vue'
import TextNode from '@/components/nodes/TextNode.vue'
import VideoNode from '@/components/nodes/VideoNode.vue'

const props = defineProps<{
  nodes: FlowNode[]
  edges: FlowEdge[]
}>()

const emit = defineEmits<{
  'node-click': [nodeId: string]
  'pane-click': []
  'node-drag-stop': [nodeId: string, position: { x: number; y: number }]
  'connect': [connection: { source: string; target: string; sourceHandle?: string; targetHandle?: string }]
  'edge-click': [edgeId: string]
}>()

// 注册自定义节点类型
const nodeTypes = {
  character: CharacterNode,
  scene: SceneNode,
  text: TextNode,
  video: VideoNode
}

const localNodes = ref<any[]>([...props.nodes])
const localEdges = ref<any[]>([...props.edges])

watch(() => props.nodes, (val) => { localNodes.value = [...val] }, { deep: true })
watch(() => props.edges, (val) => { localEdges.value = [...val] }, { deep: true })

const onNodeClick = ({ node }: any) => {
  emit('node-click', node.id)
}

const onPaneClick = () => {
  emit('pane-click')
}

const onNodeDragStop = ({ node }: any) => {
  emit('node-drag-stop', node.id, node.position)
}

const onConnect = (connection: any) => {
  emit('connect', {
    source: connection.source,
    target: connection.target,
    sourceHandle: connection.sourceHandle,
    targetHandle: connection.targetHandle
  })
}

const onEdgeClick = ({ edge }: any) => {
  emit('edge-click', edge.id)
}
</script>

<style scoped>
.free-canvas-wrapper {
  width: 100%;
  height: 100%;
  background:
    radial-gradient(circle at 20% 50%, rgba(102, 126, 234, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 80% 50%, rgba(118, 75, 162, 0.03) 0%, transparent 50%),
    #0f0f1a;
}
</style>
