import axios from 'axios'
import type { CanvasNode, NodeConnection } from '@/types'

const api = axios.create({ baseURL: '/api' })

export const nodeApi = {
  // 节点
  list: (projectId: number) => api.get<CanvasNode[]>(`/projects/${projectId}/nodes`),
  create: (projectId: number, data: Partial<CanvasNode>) => api.post<CanvasNode>(`/projects/${projectId}/nodes`, data),
  update: (projectId: number, nodeId: number, data: Partial<CanvasNode>) =>
    api.put<CanvasNode>(`/projects/${projectId}/nodes/${nodeId}`, data),
  delete: (projectId: number, nodeId: number) => api.delete(`/projects/${projectId}/nodes/${nodeId}`),

  // 连接
  listConnections: (projectId: number) => api.get<NodeConnection[]>(`/projects/${projectId}/connections`),
  createConnection: (projectId: number, data: Partial<NodeConnection>) =>
    api.post<NodeConnection>(`/projects/${projectId}/connections`, data),
  deleteConnection: (projectId: number, connId: number) =>
    api.delete(`/projects/${projectId}/connections/${connId}`)
}
