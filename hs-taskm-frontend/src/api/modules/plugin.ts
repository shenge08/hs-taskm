import apiClient from '../client'
import type { Result, DataPlugin, PluginInstance } from '@/types'

export const pluginApi = {
  // Get all plugins
  getPlugins(): Promise<Result<DataPlugin[]>> {
    return apiClient.get('/plugins')
  },

  // Get plugin by ID
  getPlugin(id: number): Promise<Result<DataPlugin>> {
    return apiClient.get(`/plugins/${id}`)
  },

  // Get plugin instances
  getPluginInstances(pluginId: number): Promise<Result<PluginInstance[]>> {
    return apiClient.get(`/plugins/${pluginId}/instances`)
  },

  // Get plugin instance logs
  getPluginInstanceLogs(instanceId: number, params?: {
    type?: string
    offset?: number
    limit?: number
  }): Promise<Result<any>> {
    return apiClient.get(`/plugin-instances/${instanceId}/logs`, { params })
  },

  // Start plugin container
  startContainer(pluginId: number): Promise<Result<string>> {
    return apiClient.post(`/plugins/${pluginId}/start`)
  },

  // Stop plugin container
  stopContainer(pluginId: number): Promise<Result<string>> {
    return apiClient.post(`/plugins/${pluginId}/stop`)
  },
}
