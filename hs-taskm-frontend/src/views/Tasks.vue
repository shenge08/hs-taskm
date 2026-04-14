<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { taskApi, strategyApi, pluginApi, listenerApi } from '@/api'
import type { Task, TaskStatistics, ContainerMetrics, Strategy, PluginInstance, ListenerInstance } from '@/types'
import { TaskStatus } from '@/types'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

const loading = ref(true)
const tasks = ref<Task[]>([])
const statistics = ref<TaskStatistics>({
  CREATED: 0,
  RUNNING: 0,
  STOPPED: 0,
  COMPLETED: 0,
  FAILED: 0,
})

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 筛选
const statusFilter = ref('')

// 任务详情抽屉
const detailDrawerVisible = ref(false)
const selectedTask = ref<Task | null>(null)
const taskMetrics = ref<ContainerMetrics | null>(null)
const taskLogs = ref<string[]>([])
const activeLogTab = ref('stdout')

// 资源监控图表
const cpuChartRef = ref<HTMLElement>()
const memoryChartRef = ref<HTMLElement>()
const networkChartRef = ref<HTMLElement>()
let cpuChart: echarts.ECharts | null = null
let memoryChart: echarts.ECharts | null = null
let networkChart: echarts.ECharts | null = null
let metricsInterval: number | null = null

// 创建任务对话框
const createDialogVisible = ref(false)
const createFormStep = ref(1)
const strategies = ref<Strategy[]>([])
const pluginInstances = ref<PluginInstance[]>([])
const listenerInstances = ref<ListenerInstance[]>([])
const createForm = ref({
  strategyId: undefined as number | undefined,
  pluginInstanceId: undefined as number | undefined,
  listenerInstanceId: undefined as number | undefined,
  parameters: {} as Record<string, any>,
})

const statusColors: Record<TaskStatus, string> = {
  [TaskStatus.CREATED]: '#64748B',
  [TaskStatus.RUNNING]: '#22C55E',
  [TaskStatus.STOPPED]: '#F59E0B',
  [TaskStatus.COMPLETED]: '#2563EB',
  [TaskStatus.FAILED]: '#DC2626',
}

const statusLabels: Record<TaskStatus, string> = {
  [TaskStatus.CREATED]: '已创建',
  [TaskStatus.RUNNING]: '运行中',
  [TaskStatus.STOPPED]: '已停止',
  [TaskStatus.COMPLETED]: '已完成',
  [TaskStatus.FAILED]: '失败',
}

const loadData = async () => {
  try {
    loading.value = true
    const [statsRes, tasksRes] = await Promise.all([
      taskApi.getTaskStatistics(),
      taskApi.getTasks({
        page: currentPage.value - 1,
        size: pageSize.value,
        status: statusFilter.value || undefined,
        sort: 'createdAt',
      }),
    ])
    statistics.value = statsRes.data || {}
    tasks.value = Array.isArray(tasksRes.data) ? tasksRes.data : []
    total.value = tasks.value.length
  } catch (error) {
    console.error('Failed to load tasks:', error)
    tasks.value = []
    statistics.value = {}
    ElMessage.error('加载任务失败')
  } finally {
    loading.value = false
  }
}

const loadStrategies = async () => {
  try {
    const res = await strategyApi.getStrategies()
    strategies.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('Failed to load strategies:', error)
    strategies.value = []
  }
}

const loadPluginInstances = async () => {
  try {
    const res = await pluginApi.getPluginInstances()
    pluginInstances.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('Failed to load plugin instances:', error)
    pluginInstances.value = []
  }
}

const loadListenerInstances = async () => {
  try {
    const res = await listenerApi.getListenerInstances()
    listenerInstances.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('Failed to load listener instances:', error)
    listenerInstances.value = []
  }
}

const handleStartTask = async (task: Task) => {
  try {
    await taskApi.startTask(task.id)
    ElMessage.success('任务启动成功')
    await loadData()
  } catch (error) {
    console.error('Failed to start task:', error)
    ElMessage.error('任务启动失败')
  }
}

const handleStopTask = async (task: Task) => {
  try {
    await taskApi.stopTask(task.id)
    ElMessage.success('任务停止成功')
    await loadData()
  } catch (error) {
    console.error('Failed to stop task:', error)
    ElMessage.error('任务停止失败')
  }
}

const handleViewDetail = async (task: Task) => {
  selectedTask.value = task
  detailDrawerVisible.value = true

  // 加载任务指标
  await loadTaskMetrics(task.id)

  // 加载任务日志
  await loadTaskLogs(task.id)

  // 如果任务正在运行，启动定时刷新
  if (task.status === TaskStatus.RUNNING) {
    startMetricsRefresh(task.id)
  }

  // 初始化图表
  setTimeout(() => {
    initCharts()
  }, 100)
}

const loadTaskMetrics = async (taskId: number) => {
  try {
    const res = await taskApi.getTaskMetrics(taskId)
    taskMetrics.value = res.data
  } catch (error) {
    console.error('Failed to load task metrics:', error)
  }
}

const loadTaskLogs = async (taskId: number) => {
  try {
    const res = await taskApi.getTaskLogs(taskId, { type: activeLogTab.value })
    taskLogs.value = res.data || []
  } catch (error) {
    console.error('Failed to load task logs:', error)
  }
}

const startMetricsRefresh = (taskId: number) => {
  if (metricsInterval) {
    clearInterval(metricsInterval)
  }
  metricsInterval = window.setInterval(() => {
    loadTaskMetrics(taskId)
    loadTaskLogs(taskId)
    updateCharts()
  }, 3000)
}

const stopMetricsRefresh = () => {
  if (metricsInterval) {
    clearInterval(metricsInterval)
    metricsInterval = null
  }
}

const initCharts = () => {
  if (!taskMetrics.value) return

  // CPU图表
  if (cpuChartRef.value) {
    cpuChart = echarts.init(cpuChartRef.value)
    updateCpuChart()
  }

  // 内存图表
  if (memoryChartRef.value) {
    memoryChart = echarts.init(memoryChartRef.value)
    updateMemoryChart()
  }

  // 网络图表
  if (networkChartRef.value) {
    networkChart = echarts.init(networkChartRef.value)
    updateNetworkChart()
  }
}

const updateCpuChart = () => {
  if (!cpuChart || !taskMetrics.value) return
  const option: EChartsOption = {
    title: { text: 'CPU使用率', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { formatter: '{b}: {c}%' },
    series: [{
      type: 'gauge',
      min: 0,
      max: 100,
      detail: { formatter: '{value}%' },
      data: [{ value: taskMetrics.value.cpuPercent.toFixed(2) }],
      axisLine: {
        lineStyle: {
          color: [[0.3, '#22C55E'], [0.7, '#F59E0B'], [1, '#DC2626']]
        }
      }
    }]
  }
  cpuChart.setOption(option)
}

const updateMemoryChart = () => {
  if (!memoryChart || !taskMetrics.value) return
  const option: EChartsOption = {
    title: { text: '内存使用率', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { formatter: '{b}: {c}%' },
    series: [{
      type: 'gauge',
      min: 0,
      max: 100,
      detail: { formatter: '{value}%' },
      data: [{ value: taskMetrics.value.memoryPercent.toFixed(2) }],
      axisLine: {
        lineStyle: {
          color: [[0.3, '#22C55E'], [0.7, '#F59E0B'], [1, '#DC2626']]
        }
      }
    }]
  }
  memoryChart.setOption(option)
}

const updateNetworkChart = () => {
  if (!networkChart || !taskMetrics.value) return
  const option: EChartsOption = {
    title: { text: '网络I/O', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    legend: { data: ['接收(RX)', '发送(TX)'], bottom: 0 },
    xAxis: { type: 'category', data: ['网络'] },
    yAxis: { type: 'value', axisLabel: { formatter: '{value} MB' } },
    series: [
      {
        name: '接收(RX)',
        type: 'bar',
        data: [(taskMetrics.value.networkRx / 1024 / 1024).toFixed(2)],
        itemStyle: { color: '#2563EB' }
      },
      {
        name: '发送(TX)',
        type: 'bar',
        data: [(taskMetrics.value.networkTx / 1024 / 1024).toFixed(2)],
        itemStyle: { color: '#22C55E' }
      }
    ]
  }
  networkChart.setOption(option)
}

const updateCharts = () => {
  updateCpuChart()
  updateMemoryChart()
  updateNetworkChart()
}

const handleLogTabChange = (tabName: string) => {
  activeLogTab.value = tabName
  if (selectedTask.value) {
    loadTaskLogs(selectedTask.value.id)
  }
}

const handleDrawerClose = () => {
  stopMetricsRefresh()
  if (cpuChart) {
    cpuChart.dispose()
    cpuChart = null
  }
  if (memoryChart) {
    memoryChart.dispose()
    memoryChart = null
  }
  if (networkChart) {
    networkChart.dispose()
    networkChart = null
  }
  detailDrawerVisible.value = false
  selectedTask.value = null
  taskMetrics.value = null
  taskLogs.value = []
}

const handleCreateTask = () => {
  createFormStep.value = 1
  createForm.value = {
    strategyId: undefined,
    pluginInstanceId: undefined,
    listenerInstanceId: undefined,
    parameters: {},
  }
  createDialogVisible.value = true

  // 加载选项数据
  loadStrategies()
  loadPluginInstances()
  loadListenerInstances()
}

const handleCreateDialogClose = () => {
  createDialogVisible.value = false
  createFormStep.value = 1
}

const handleCreateNext = () => {
  if (createFormStep.value < 3) {
    createFormStep.value++
  }
}

const handleCreatePrev = () => {
  if (createFormStep.value > 1) {
    createFormStep.value--
  }
}

const handleCreateSubmit = async () => {
  try {
    await taskApi.createTask(createForm.value)
    ElMessage.success('任务创建成功')
    createDialogVisible.value = false
    await loadData()
  } catch (error) {
    console.error('Failed to create task:', error)
    ElMessage.error('任务创建失败')
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadData()
}

const handleStatusFilter = () => {
  currentPage.value = 1
  loadData()
}

onMounted(() => {
  loadData()
})

onUnmounted(() => {
  stopMetricsRefresh()
})
</script>

<template>
  <div v-loading="loading" class="space-y-6">
    <!-- Statistics Cards -->
    <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
      <div
        v-for="(count, status) in statistics"
        :key="status"
        class="bg-card rounded-lg p-4 border border-border hover:shadow-md transition-shadow cursor-pointer"
        @click="statusFilter = status === statusFilter ? '' : status; handleStatusFilter()"
      >
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-muted">{{ statusLabels[status as TaskStatus] }}</p>
            <p class="text-2xl font-bold mt-1" :style="{ color: statusColors[status as TaskStatus] }">
              {{ count }}
            </p>
          </div>
          <div class="w-10 h-10 rounded-full flex items-center justify-center"
               :style="{ backgroundColor: `${statusColors[status as TaskStatus]}20` }">
            <el-icon :size="20" :color="statusColors[status as TaskStatus]">
              <component :is="status === TaskStatus.RUNNING ? 'VideoPlay' : 'VideoPause'" />
            </el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- Filter and Actions -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-4">
        <el-select
          v-model="statusFilter"
          placeholder="筛选状态"
          clearable
          @change="handleStatusFilter"
          style="width: 150px"
        >
          <el-option label="全部" value="" />
          <el-option label="已创建" value="CREATED" />
          <el-option label="运行中" value="RUNNING" />
          <el-option label="已停止" value="STOPPED" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="失败" value="FAILED" />
        </el-select>
        <el-button type="primary" @click="handleCreateTask">
          <el-icon class="mr-1"><Plus /></el-icon>
          创建任务
        </el-button>
      </div>
      <div class="text-sm text-muted">
        共 {{ total }} 条任务
      </div>
    </div>

    <!-- Tasks Table -->
    <div class="bg-card rounded-lg border border-border">
      <el-table :data="tasks" class="w-full">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="strategyId" label="策略ID" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :color="statusColors[row.status]" size="small">
              {{ statusLabels[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="插件实例" width="120">
          <template #default="{ row }">
            <span class="text-sm">{{ row.pluginInstanceId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="监听器实例" width="120">
          <template #default="{ row }">
            <span class="text-sm">{{ row.listenerInstanceId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            <span class="text-sm">{{ new Date(row.createdAt).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === TaskStatus.CREATED || row.status === TaskStatus.STOPPED"
              type="primary"
              size="small"
              @click="handleStartTask(row)"
            >
              启动
            </el-button>
            <el-button
              v-if="row.status === TaskStatus.RUNNING"
              type="warning"
              size="small"
              @click="handleStopTask(row)"
            >
              停止
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="p-4 border-t border-border flex items-center justify-end">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- Task Detail Drawer -->
    <el-drawer
      v-model="detailDrawerVisible"
      title="任务详情"
      size="60%"
      @close="handleDrawerClose"
    >
      <div v-if="selectedTask" class="space-y-6">
        <!-- Task Info -->
        <div class="bg-card rounded-lg p-4 border border-border">
          <h3 class="font-heading font-semibold mb-3">基本信息</h3>
          <div class="grid grid-cols-2 gap-4 text-sm">
            <div><span class="text-muted">任务ID:</span> {{ selectedTask.id }}</div>
            <div><span class="text-muted">策略ID:</span> {{ selectedTask.strategyId }}</div>
            <div><span class="text-muted">状态:</span>
              <el-tag :color="statusColors[selectedTask.status]" size="small">
                {{ statusLabels[selectedTask.status] }}
              </el-tag>
            </div>
            <div><span class="text-muted">插件实例:</span> {{ selectedTask.pluginInstanceId || '-' }}</div>
            <div><span class="text-muted">监听器实例:</span> {{ selectedTask.listenerInstanceId || '-' }}</div>
            <div><span class="text-muted">创建时间:</span> {{ new Date(selectedTask.createdAt).toLocaleString() }}</div>
          </div>
        </div>

        <!-- Resource Monitoring -->
        <div class="bg-card rounded-lg p-4 border border-border">
          <h3 class="font-heading font-semibold mb-3">资源监控</h3>
          <div class="grid grid-cols-3 gap-4">
            <div ref="cpuChartRef" class="h-48"></div>
            <div ref="memoryChartRef" class="h-48"></div>
            <div ref="networkChartRef" class="h-48"></div>
          </div>
          <div v-if="taskMetrics" class="grid grid-cols-4 gap-4 mt-4 text-sm">
            <div class="text-center">
              <p class="text-muted">磁盘读取</p>
              <p class="font-semibold">{{ (taskMetrics.blockRead / 1024 / 1024).toFixed(2) }} MB</p>
            </div>
            <div class="text-center">
              <p class="text-muted">磁盘写入</p>
              <p class="font-semibold">{{ (taskMetrics.blockWrite / 1024 / 1024).toFixed(2) }} MB</p>
            </div>
            <div class="text-center">
              <p class="text-muted">内存使用</p>
              <p class="font-semibold">{{ (taskMetrics.memoryUsage / 1024 / 1024).toFixed(2) }} MB</p>
            </div>
            <div class="text-center">
              <p class="text-muted">内存限制</p>
              <p class="font-semibold">{{ (taskMetrics.memoryLimit / 1024 / 1024).toFixed(2) }} MB</p>
            </div>
          </div>
        </div>

        <!-- Logs Viewer -->
        <div class="bg-card rounded-lg p-4 border border-border">
          <h3 class="font-heading font-semibold mb-3">日志查看器</h3>
          <el-tabs v-model="activeLogTab" @tab-change="handleLogTabChange">
            <el-tab-pane label="标准输出" name="stdout">
              <div class="bg-gray-900 rounded p-3 h-64 overflow-auto font-mono text-sm text-green-400">
                <div v-if="taskLogs.length === 0" class="text-gray-500">暂无日志</div>
                <div v-else v-for="(log, index) in taskLogs" :key="index">{{ log }}</div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="错误输出" name="stderr">
              <div class="bg-gray-900 rounded p-3 h-64 overflow-auto font-mono text-sm text-red-400">
                <div v-if="taskLogs.length === 0" class="text-gray-500">暂无日志</div>
                <div v-else v-for="(log, index) in taskLogs" :key="index">{{ log }}</div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="自定义日志" name="custom">
              <div class="bg-gray-900 rounded p-3 h-64 overflow-auto font-mono text-sm text-blue-400">
                <div v-if="taskLogs.length === 0" class="text-gray-500">暂无日志</div>
                <div v-else v-for="(log, index) in taskLogs" :key="index">{{ log }}</div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </el-drawer>

    <!-- Create Task Dialog -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建任务"
      width="600px"
      @close="handleCreateDialogClose"
    >
      <el-steps :active="createFormStep" align-center class="mb-6">
        <el-step title="选择策略" />
        <el-step title="选择实例" />
        <el-step title="配置参数" />
      </el-steps>

      <!-- Step 1: Select Strategy -->
      <div v-show="createFormStep === 1">
        <el-form label-width="100px">
          <el-form-item label="执行策略">
            <el-select v-model="createForm.strategyId" placeholder="请选择策略" style="width: 100%">
              <el-option
                v-for="strategy in strategies"
                :key="strategy.id"
                :label="`${strategy.name} (${strategy.language})`"
                :value="strategy.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <!-- Step 2: Select Instances -->
      <div v-show="createFormStep === 2">
        <el-form label-width="100px">
          <el-form-item label="插件实例">
            <el-select v-model="createForm.pluginInstanceId" placeholder="请选择插件实例（可选）" clearable style="width: 100%">
              <el-option
                v-for="instance in pluginInstances"
                :key="instance.id"
                :label="instance.name"
                :value="instance.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="监听器实例">
            <el-select v-model="createForm.listenerInstanceId" placeholder="请选择监听器实例（可选）" clearable style="width: 100%">
              <el-option
                v-for="instance in listenerInstances"
                :key="instance.id"
                :label="instance.name"
                :value="instance.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <!-- Step 3: Configure Parameters -->
      <div v-show="createFormStep === 3">
        <el-form label-width="100px">
          <el-form-item label="参数配置">
            <el-input
              v-model="createForm.parameters"
              type="textarea"
              :rows="6"
              placeholder='请输入JSON格式的参数，例如: {"key": "value"}'
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="handleCreateDialogClose">取消</el-button>
        <el-button v-if="createFormStep > 1" @click="handleCreatePrev">上一步</el-button>
        <el-button v-if="createFormStep < 3" type="primary" @click="handleCreateNext">下一步</el-button>
        <el-button v-if="createFormStep === 3" type="primary" @click="handleCreateSubmit">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.el-table {
  --el-table-border-color: theme('colors.border');
  --el-table-bg-color: theme('colors.card');
  --el-table-header-bg-color: theme('colors.muted');
}

:deep(.el-table__header) {
  font-family: 'Fira Code', monospace;
  font-weight: 600;
}

:deep(.el-table__body tr:hover > td) {
  background-color: #F8FAFC;
}
</style>
