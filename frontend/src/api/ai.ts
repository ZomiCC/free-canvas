import axios from 'axios'
import type { GenerateRequest, AiTask } from '@/types'

const api = axios.create({ baseURL: '/api' })

export const aiApi = {
  generateImage: (data: GenerateRequest) => api.post<AiTask>('/ai/generate-image', data),
  generateVideo: (data: GenerateRequest) => api.post<AiTask>('/ai/generate-video', data),
  getTaskStatus: (taskId: number) => api.get<AiTask>(`/ai/task/${taskId}`),
  health: () => api.get('/ai/health')
}
