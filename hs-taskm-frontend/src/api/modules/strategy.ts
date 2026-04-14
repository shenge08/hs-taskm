import apiClient from '../client'
import type { Result, Strategy } from '@/types'

export const strategyApi = {
  // Get all strategies
  getStrategies(params?: {
    language?: string
  }): Promise<Result<Strategy[]>> {
    return apiClient.get('/strategies', { params })
  },

  // Get strategy by ID
  getStrategy(id: number): Promise<Result<Strategy>> {
    return apiClient.get(`/strategies/${id}`)
  },

  // Create strategy
  createStrategy(data: Partial<Strategy>): Promise<Result<Strategy>> {
    return apiClient.post('/strategies', data)
  },

  // Update strategy
  updateStrategy(id: number, data: Partial<Strategy>): Promise<Result<Strategy>> {
    return apiClient.put(`/strategies/${id}`, data)
  },

  // Delete strategy
  deleteStrategy(id: number): Promise<Result<string>> {
    return apiClient.delete(`/strategies/${id}`)
  },
}
