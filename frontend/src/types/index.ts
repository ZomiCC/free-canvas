/** 节点类型 */
export enum NodeType {
  CHARACTER = 'CHARACTER',
  SCENE = 'SCENE',
  TEXT = 'TEXT',
  VIDEO = 'VIDEO'
}

/** 视频节点生成状态 */
export enum VideoStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

// ==================== 项目 ====================

export interface Project {
  id?: number
  name: string
  description?: string
  thumbnail?: string
  createdAt?: string
  updatedAt?: string
}

// ==================== 节点 ====================

export interface CanvasNode {
  id?: number
  projectId: number
  nodeType: NodeType
  label: string
  posX: number
  posY: number
  width: number
  height: number
  nodeData?: string  // JSON
  style?: string     // JSON
  createdAt?: string
  updatedAt?: string
}

/** 角色节点数据 */
export interface CharacterData {
  name: string
  description: string
  age: number
  gender: string
  personality: string
  avatarUrl: string
  voiceTone: string
}

/** 场景节点数据 */
export interface SceneData {
  name: string
  description: string
  location: string
  timeOfDay: string
  atmosphere: string
  backgroundUrl: string
  weather: string
}

/** 文字/台词节点数据 */
export interface TextData {
  title: string
  content: string
  speaker: string
  emotion: string
  fontSize: number
}

/** 视频节点数据 */
export interface VideoData {
  title: string
  description: string
  prompt: string
  videoUrl: string
  thumbnailUrl: string
  duration: number
  status: VideoStatus
}

// ==================== 连接 ====================

export interface NodeConnection {
  id?: number
  projectId: number
  sourceNodeId: number
  targetNodeId: number
  sourceHandle?: string
  targetHandle?: string
  label?: string
  style?: string
}

// ==================== AI 生成 ====================

export interface GenerateRequest {
  prompt: string
  projectId?: number
  nodeId?: number
  negativePrompt?: string
  width?: number
  height?: number
  duration?: number
  style?: string
}

export interface AiTask {
  id?: number
  projectId?: number
  nodeId?: number
  taskType: 'IMAGE' | 'VIDEO'
  prompt: string
  status: VideoStatus
  resultUrl?: string
  errorMsg?: string
}

// ==================== WebSocket 消息 ====================

export interface WsMessage {
  type: string
  projectId: number
  data: any
  taskId?: number
  nodeId?: number
  status?: string
  resultUrl?: string
  errorMsg?: string
}

// ==================== Vue Flow 兼容 ====================

export interface FlowNode {
  id: string
  type: string
  position: { x: number; y: number }
  data: any
  style?: Record<string, any>
}

export interface FlowEdge {
  id: string
  source: string
  target: string
  sourceHandle?: string
  targetHandle?: string
  label?: string
  style?: Record<string, any>
}
