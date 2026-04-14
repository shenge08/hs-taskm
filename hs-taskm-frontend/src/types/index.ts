// Task Status
export enum TaskStatus {
  CREATED = 'CREATED',
  RUNNING = 'RUNNING',
  STOPPED = 'STOPPED',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}

// Base Response
export interface Result<T> {
  code: number
  message: string
  data: T
}

// Strategy
export interface Strategy {
  id: number
  name: string
  description: string
  language: string
  code: string
  configParameters: string
  parameterDefaults: string
  dockerImageId: string
  createdAt: string
  updatedAt: string
}

// Data Plugin
export interface DataPlugin {
  id: number
  name: string
  description: string
  pluginType: string
  language: string
  code: string
  configParameters: string
  imageName: string
  createdAt: string
  updatedAt: string
}

// Plugin Instance
export interface PluginInstance {
  id: number
  pluginId: number
  name: string
  isDefault: boolean
  config: string
  containerId: string
  status: string
  createdAt: string
  updatedAt: string
}

// Listener
export interface Listener {
  id: number
  name: string
  description: string
  eventType: string
  language: string
  code: string
  imageName: string
  createdAt: string
  updatedAt: string
}

// Listener Instance
export interface ListenerInstance {
  id: number
  listenerId: number
  name: string
  isDefault: boolean
  config: string
  containerId: string
  status: string
  createdAt: string
  updatedAt: string
}

// Task
export interface Task {
  id: number
  strategyId: number
  pluginInstanceId?: number
  listenerInstanceId?: number
  status: TaskStatus
  pluginEndpoint?: string
  listenerEndpoint?: string
  createdAt: string
  startedAt?: string
  completedAt?: string
}

// Container Metrics
export interface ContainerMetrics {
  cpuPercent: number
  memoryUsage: number
  memoryLimit: number
  memoryPercent: number
  networkRx: number
  networkTx: number
  blockRead: number
  blockWrite: number
}

// Task Statistics
export interface TaskStatistics {
  CREATED: number
  RUNNING: number
  STOPPED: number
  COMPLETED: number
  FAILED: number
}
