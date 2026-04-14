import apiClient from '../client'
import type { Result, Listener, ListenerInstance } from '@/types'

export const listenerApi = {
  // Get all listeners
  getListeners(): Promise<Result<Listener[]>> {
    return apiClient.get('/listeners')
  },

  // Get listener by ID
  getListener(id: number): Promise<Result<Listener>> {
    return apiClient.get(`/listeners/${id}`)
  },

  // Get listener instances
  getListenerInstances(listenerId: number): Promise<Result<ListenerInstance[]>> {
    return apiClient.get(`/listeners/${listenerId}/instances`)
  },

  // Get listener instance logs
  getListenerInstanceLogs(instanceId: number, params?: {
    type?: string
    offset?: number
    limit?: number
  }): Promise<Result<any>> {
    return apiClient.get(`/listener-instances/${instanceId}/logs`, { params })
  },

  // Start listener container
  startContainer(listenerId: number): Promise<Result<string>> {
    return apiClient.post(`/listeners/${listenerId}/start`)
  },

  // Stop listener container
  stopContainer(listenerId: number): Promise<Result<string>> {
    return apiClient.post(`/listeners/${listenerId}/stop`)
  },

  // Start listener instance container
  startListenerInstanceContainer(instanceId: number): Promise<Result<string>> {
    return apiClient.post(`/listener-instances/${instanceId}/start`)
  },

  // Stop listener instance container
  stopListenerInstanceContainer(instanceId: number): Promise<Result<string>> {
    return apiClient.post(`/listener-instances/${instanceId}/stop`)
  },

  // Restart listener instance container
  restartListenerInstanceContainer(instanceId: number): Promise<Result<string>> {
    return apiClient.post(`/listener-instances/${instanceId}/restart`)
  },
}
