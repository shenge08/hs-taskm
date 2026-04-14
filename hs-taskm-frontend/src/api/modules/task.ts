import apiClient from '../client'
import type { Result, Task, TaskStatistics, ContainerMetrics } from '@/types'

export const taskApi = {
  // Get all tasks (paginated)
  getTasks(params: {
    page?: number
    size?: number
    status?: string
    sort?: string
  }): Promise<Result<Task[]>> {
    return apiClient.get('/tasks', { params })
  },

  // Get task statistics
  getTaskStatistics(): Promise<Result<TaskStatistics>> {
    return apiClient.get('/tasks/statistics')
  },

  // Get task by ID
  getTask(id: number): Promise<Result<Task>> {
    return apiClient.get(`/tasks/${id}`)
  },

  // Create task
  createTask(data: {
    strategyId: number
    pluginInstanceId?: number
    listenerInstanceId?: number
    parameters?: Record<string, any>
  }): Promise<Result<Task>> {
    return apiClient.post('/tasks', data)
  },

  // Start task
  startTask(id: number): Promise<Result<any>> {
    return apiClient.post(`/tasks/${id}/start`)
  },

  // Stop task
  stopTask(id: number): Promise<Result<string>> {
    return apiClient.post(`/tasks/${id}/stop`)
  },

  // Get task metrics
  getTaskMetrics(id: number): Promise<Result<ContainerMetrics>> {
    return apiClient.get(`/tasks/${id}/metrics`)
  },

  // Get task logs
  getTaskLogs(id: number, params?: {
    type?: string
    offset?: number
    limit?: number
  }): Promise<Result<any>> {
    return apiClient.get(`/tasks/${id}/logs`, { params })
  },
}
