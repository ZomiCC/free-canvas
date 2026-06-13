import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { CanvasNode, NodeConnection, FlowNode, FlowEdge, WsMessage } from '@/types'
import { nodeApi } from '@/api/node'
import { NodeType } from '@/types'

export const useCanvasStore = defineStore('canvas', () => {
  const nodes = ref<CanvasNode[]>([])
  const connections = ref<NodeConnection[]>([])
  const selectedNodeId = ref<number | null>(null)
  const wsConnected = ref(false)

  // 转换为 Vue Flow 格式
  const flowNodes = computed<FlowNode[]>(() =>
    nodes.value.map(n => ({
      id: String(n.id),
      type: n.nodeType.toLowerCase(),
      position: { x: n.posX, y: n.posY },
      data: {
        ...parseNodeData(n),
        label: n.label,
        nodeType: n.nodeType,
        style: n.style ? JSON.parse(n.style) : {}
      }
    }))
  )

  const flowEdges = computed<FlowEdge[]>(() =>
    connections.value.map(c => ({
      id: String(c.id),
      source: String(c.sourceNodeId),
      target: String(c.targetNodeId),
      sourceHandle: c.sourceHandle,
      targetHandle: c.targetHandle,
      label: c.label,
      style: c.style ? JSON.parse(c.style) : { stroke: '#666', strokeWidth: 2 }
    }))
  )

  const selectedNode = computed(() =>
    nodes.value.find(n => n.id === selectedNodeId.value) || null
  )

  // ==================== 数据加载 ====================

  const loadNodes = async (projectId: number) => {
    const res = await nodeApi.list(projectId)
    nodes.value = res.data
  }

  const loadConnections = async (projectId: number) => {
    const res = await nodeApi.listConnections(projectId)
    connections.value = res.data
  }

  const loadAll = async (projectId: number) => {
    await Promise.all([loadNodes(projectId), loadConnections(projectId)])
  }

  // ==================== 节点操作 ====================

  const addNode = async (projectId: number, nodeType: NodeType, x: number, y: number) => {
    const defaultData = getDefaultData(nodeType)
    const newNode: Partial<CanvasNode> = {
      projectId,
      nodeType,
      label: getDefaultLabel(nodeType),
      posX: x,
      posY: y,
      width: nodeType === NodeType.VIDEO ? 300 : 200,
      height: nodeType === NodeType.VIDEO ? 240 : 180,
      nodeData: JSON.stringify(defaultData)
    }
    const res = await nodeApi.create(projectId, newNode)
    nodes.value.push(res.data)
    return res.data
  }

  const updateNodePosition = async (nodeId: string, x: number, y: number) => {
    const id = Number(nodeId)
    const node = nodes.value.find(n => n.id === id)
    if (!node) return
    node.posX = x
    node.posY = y
    await nodeApi.update(node.projectId, id, { posX: x, posY: y } as any)
  }

  const updateNodeData = async (nodeId: number, nodeData: any) => {
    const node = nodes.value.find(n => n.id === nodeId)
    if (!node) return
    node.nodeData = typeof nodeData === 'string' ? nodeData : JSON.stringify(nodeData)
    await nodeApi.update(node.projectId, nodeId, { nodeData: node.nodeData } as any)
  }

  const removeNode = async (nodeId: string) => {
    const id = Number(nodeId)
    const node = nodes.value.find(n => n.id === id)
    if (!node) return
    await nodeApi.delete(node.projectId, id)
    nodes.value = nodes.value.filter(n => n.id !== id)
    connections.value = connections.value.filter(
      c => c.sourceNodeId !== id && c.targetNodeId !== id
    )
    if (selectedNodeId.value === id) selectedNodeId.value = null
  }

  // ==================== 连接操作 ====================

  const addConnection = async (conn: { source: string; target: string; sourceHandle?: string; targetHandle?: string }) => {
    const projectId = nodes.value[0]?.projectId
    if (!projectId) return
    const newConn: Partial<NodeConnection> = {
      projectId,
      sourceNodeId: Number(conn.source),
      targetNodeId: Number(conn.target),
      sourceHandle: conn.sourceHandle,
      targetHandle: conn.targetHandle
    }
    const res = await nodeApi.createConnection(projectId, newConn)
    connections.value.push(res.data)
    return res.data
  }

  const removeConnection = async (connId: string) => {
    const id = Number(connId)
    const conn = connections.value.find(c => c.id === id)
    if (!conn) return
    await nodeApi.deleteConnection(conn.projectId, id)
    connections.value = connections.value.filter(c => c.id !== id)
  }

  // ==================== 选中 ====================

  const selectNode = (nodeId: number | null) => {
    selectedNodeId.value = nodeId
  }

  // ==================== WebSocket ====================

  const handleWsMessage = (msg: WsMessage) => {
    switch (msg.type) {
      case 'NODE_CREATED':
        if (msg.data && !nodes.value.find(n => n.id === msg.data.id)) {
          nodes.value.push(msg.data)
        }
        break
      case 'NODE_UPDATED':
        if (msg.data) {
          const idx = nodes.value.findIndex(n => n.id === msg.data.id)
          if (idx >= 0) nodes.value[idx] = msg.data
        }
        break
      case 'NODE_DELETED':
        if (msg.data?.nodeId) {
          nodes.value = nodes.value.filter(n => n.id !== msg.data.nodeId)
          connections.value = connections.value.filter(
            c => c.sourceNodeId !== msg.data.nodeId && c.targetNodeId !== msg.data.nodeId
          )
        }
        break
      case 'CONN_CREATED':
        if (msg.data && !connections.value.find(c => c.id === msg.data.id)) {
          connections.value.push(msg.data)
        }
        break
      case 'CONN_DELETED':
        if (msg.data?.connectionId) {
          connections.value = connections.value.filter(c => c.id !== msg.data.connectionId)
        }
        break
      case 'AI_TASK_UPDATE':
        // 更新视频节点状态
        if (msg.nodeId) {
          const node = nodes.value.find(n => n.id === msg.nodeId)
          if (node && msg.resultUrl) {
            const data = parseNodeData(node)
            data.videoUrl = msg.resultUrl
            data.status = msg.status
            node.nodeData = JSON.stringify(data)
          }
        }
        break
    }
  }

  // ==================== 辅助 ====================

  function parseNodeData(node: CanvasNode): any {
    try {
      return node.nodeData ? JSON.parse(node.nodeData) : {}
    } catch {
      return {}
    }
  }

  function getDefaultLabel(type: NodeType): string {
    switch (type) {
      case NodeType.CHARACTER: return '新角色'
      case NodeType.SCENE: return '新场景'
      case NodeType.TEXT: return '新台词'
      case NodeType.VIDEO: return '新视频'
    }
  }

  function getDefaultData(type: NodeType): any {
    switch (type) {
      case NodeType.CHARACTER:
        return { name: '新角色', description: '', age: 25, gender: '男', personality: '', avatarUrl: '', voiceTone: '' }
      case NodeType.SCENE:
        return { name: '新场景', description: '', location: '', timeOfDay: '白天', atmosphere: '', backgroundUrl: '', weather: '晴' }
      case NodeType.TEXT:
        return { title: '新台词', content: '请输入台词内容...', speaker: '', emotion: '平静', fontSize: 16 }
      case NodeType.VIDEO:
        return { title: '新视频', description: '', prompt: '', videoUrl: '', thumbnailUrl: '', duration: 5, status: 'PENDING' }
    }
  }

  return {
    nodes, connections, selectedNodeId, selectedNode, wsConnected,
    flowNodes, flowEdges,
    loadAll, loadNodes, loadConnections,
    addNode, updateNodePosition, updateNodeData, removeNode,
    addConnection, removeConnection,
    selectNode, handleWsMessage
  }
})
