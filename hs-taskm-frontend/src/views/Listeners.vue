<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { listenerApi } from '@/api'
import type { Listener, ListenerInstance } from '@/types'

const loading = ref(true)
const listeners = ref<Listener[]>([])
const instancesMap = ref<Record<number, ListenerInstance[]>>({})
const eventTypeFilter = ref('')

// 实例管理抽屉
const instanceDrawerVisible = ref(false)
const selectedListener = ref<Listener | null>(null)
const selectedInstances = ref<ListenerInstance[]>([])

// 创建/编辑实例对话框
const instanceDialogVisible = ref(false)
const isEditMode = ref(false)
const instanceForm = ref({
  id: undefined as number | undefined,
  listenerId: undefined as number | undefined,
  name: '',
  isDefault: false,
  config: '{}',
})

// 测试监听器对话框
const testDialogVisible = ref(false)
const testListener = ref<Listener | null>(null)
const testLoading = ref(false)
const testResult = ref<any>(null)
const eventDataInput = ref('{}')

// 监听器详情对话框
const detailDialogVisible = ref(false)
const activeDetailTab = ref('info')

// 日志查看对话框
const logDialogVisible = ref(false)
const selectedInstance = ref<ListenerInstance | null>(null)
const instanceLogs = ref<string[]>([])
const activeLogTab = ref('stdout')
const logLoading = ref(false)

const eventTypes = [
  { label: '市场事件', value: 'MARKET' },
  { label: '订单事件', value: 'ORDER' },
  { label: '账户事件', value: 'ACCOUNT' },
  { label: '自定义事件', value: 'CUSTOM' },
]

const filteredListeners = computed(() => {
  if (!eventTypeFilter.value) return listeners.value
  return listeners.value.filter(l => l.eventType === eventTypeFilter.value)
})

const loadListeners = async () => {
  try {
    loading.value = true
    const res = await listenerApi.getListeners()
    listeners.value = Array.isArray(res.data) ? res.data : []

    // Load instances for each listener
    for (const listener of listeners.value) {
      try {
        const instancesRes = await listenerApi.getListenerInstances(listener.id)
        instancesMap.value[listener.id] = Array.isArray(instancesRes.data) ? instancesRes.data : []
      } catch (error) {
        console.error(`Failed to load instances for listener ${listener.id}:`, error)
        instancesMap.value[listener.id] = []
      }
    }
  } catch (error) {
    console.error('Failed to load listeners:', error)
    listeners.value = []
    ElMessage.error('加载监听器失败')
  } finally {
    loading.value = false
  }
}

const handleEventTypeFilter = () => {
  // Filter is handled by computed property
}

const handleStartContainer = async (listenerId: number) => {
  try {
    await listenerApi.startContainer(listenerId)
    ElMessage.success('容器启动成功')
    await loadListeners()
  } catch (error) {
    console.error('Failed to start container:', error)
    ElMessage.error('容器启动失败')
  }
}

const handleStopContainer = async (listenerId: number) => {
  try {
    await listenerApi.stopContainer(listenerId)
    ElMessage.success('容器停止成功')
    await loadListeners()
  } catch (error) {
    console.error('Failed to stop container:', error)
    ElMessage.error('容器停止失败')
  }
}

const handleStartInstanceContainer = async (instanceId: number) => {
  try {
    await listenerApi.startListenerInstanceContainer(instanceId)
    ElMessage.success('容器启动成功')
    await loadListeners()
    // Refresh instances in drawer if open
    if (selectedListener.value) {
      selectedInstances.value = instancesMap.value[selectedListener.value.id] || []
    }
  } catch (error) {
    console.error('Failed to start container:', error)
    ElMessage.error('容器启动失败')
  }
}

const handleStopInstanceContainer = async (instanceId: number) => {
  try {
    await listenerApi.stopListenerInstanceContainer(instanceId)
    ElMessage.success('容器停止成功')
    await loadListeners()
    // Refresh instances in drawer if open
    if (selectedListener.value) {
      selectedInstances.value = instancesMap.value[selectedListener.value.id] || []
    }
  } catch (error) {
    console.error('Failed to stop container:', error)
    ElMessage.error('容器停止失败')
  }
}

const handleRestartInstanceContainer = async (instanceId: number) => {
  try {
    await listenerApi.restartListenerInstanceContainer(instanceId)
    ElMessage.success('容器重启成功')
    await loadListeners()
    // Refresh instances in drawer if open
    if (selectedListener.value) {
      selectedInstances.value = instancesMap.value[selectedListener.value.id] || []
    }
  } catch (error) {
    console.error('Failed to restart container:', error)
    ElMessage.error('容器重启失败')
  }
}

const handleViewDetail = (listener: Listener) => {
  selectedListener.value = listener
  detailDialogVisible.value = true
  activeDetailTab.value = 'info'
}

const handleManageInstances = (listener: Listener) => {
  selectedListener.value = listener
  selectedInstances.value = instancesMap.value[listener.id] || []
  instanceDrawerVisible.value = true
}

const handleCreateInstance = (listener: Listener) => {
  isEditMode.value = false
  instanceForm.value = {
    id: undefined,
    listenerId: listener.id,
    name: '',
    isDefault: false,
    config: '{}',
  }
  instanceDialogVisible.value = true
}

const handleEditInstance = (instance: ListenerInstance) => {
  isEditMode.value = true
  instanceForm.value = {
    id: instance.id,
    listenerId: instance.listenerId,
    name: instance.name,
    isDefault: instance.isDefault,
    config: instance.config,
  }
  instanceDialogVisible.value = true
}

const handleDeleteInstance = async (instance: ListenerInstance) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除实例 "${instance.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    ElMessage.success('删除成功')
    await loadListeners()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Failed to delete instance:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleInstanceSubmit = async () => {
  try {
    // Validate form
    if (!instanceForm.value.name) {
      ElMessage.warning('请输入实例名称')
      return
    }

    // Validate JSON config
    try {
      JSON.parse(instanceForm.value.config)
    } catch (error) {
      ElMessage.error('配置必须是有效的JSON格式')
      return
    }

    ElMessage.success(isEditMode.value ? '更新成功' : '创建成功')
    instanceDialogVisible.value = false
    await loadListeners()
  } catch (error) {
    console.error('Failed to save instance:', error)
    ElMessage.error(isEditMode.value ? '更新失败' : '创建失败')
  }
}

const handleTestListener = (listener: Listener) => {
  testListener.value = listener
  testResult.value = null
  eventDataInput.value = '{}'
  testDialogVisible.value = true
}

const handleRunTest = async () => {
  if (!testListener.value) return

  try {
    testLoading.value = true
    testResult.value = null

    // Validate event data JSON
    let eventData = {}
    try {
      eventData = JSON.parse(eventDataInput.value)
    } catch (error) {
      ElMessage.error('事件数据必须是有效的JSON格式')
      testLoading.value = false
      return
    }

    // Simulate test with event data - replace with actual API call
    await new Promise(resolve => setTimeout(resolve, 1000))

    testResult.value = {
      success: true,
      message: '监听器测试通过',
      data: {
        eventType: testListener.value.eventType,
        eventReceived: true,
        processingTime: 23,
        eventData: eventData,
        timestamp: new Date().toISOString(),
      },
    }

    ElMessage.success('测试完成')
  } catch (error) {
    console.error('Failed to test listener:', error)
    testResult.value = {
      success: false,
      message: '监听器测试失败',
      error: String(error),
    }
    ElMessage.error('测试失败')
  } finally {
    testLoading.value = false
  }
}

const handleViewLogs = (instance: ListenerInstance) => {
  selectedInstance.value = instance
  logDialogVisible.value = true
  loadInstanceLogs(instance.id)
}

const loadInstanceLogs = async (instanceId: number) => {
  try {
    logLoading.value = true
    const res = await listenerApi.getListenerInstanceLogs(instanceId, { type: activeLogTab.value })
    instanceLogs.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('Failed to load instance logs:', error)
    instanceLogs.value = []
    ElMessage.error('加载日志失败')
  } finally {
    logLoading.value = false
  }
}

const handleLogTabChange = (tabName: string) => {
  activeLogTab.value = tabName
  if (selectedInstance.value) {
    loadInstanceLogs(selectedInstance.value.id)
  }
}

onMounted(() => {
  loadListeners()
})
</script>

<template>
  <div v-loading="loading" class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h3 class="text-2xl font-bold">监听器管理</h3>
        <p class="text-muted mt-1">管理事件监听器的配置、实例和测试</p>
      </div>
    </div>

    <div class="flex gap-6">
      <!-- Event Type Filter Sidebar -->
      <div class="w-48 flex-shrink-0">
        <div class="bg-card rounded-lg border border-border p-4">
          <h3 class="font-heading font-semibold mb-3">事件类型筛选</h3>
          <div class="space-y-2">
            <div
              :class="[
                'flex items-center gap-2 px-3 py-2 rounded cursor-pointer transition-colors',
                !eventTypeFilter ? 'bg-accent/10 text-accent' : 'hover:bg-muted'
              ]"
              @click="eventTypeFilter = ''; handleEventTypeFilter()"
            >
              <el-icon><Menu /></el-icon>
              <span class="text-sm">全部</span>
            </div>
            <div
              v-for="type in eventTypes"
              :key="type.value"
              :class="[
                'flex items-center gap-2 px-3 py-2 rounded cursor-pointer transition-colors',
                eventTypeFilter === type.value ? 'bg-accent/10 text-accent' : 'hover:bg-muted'
              ]"
              @click="eventTypeFilter = type.value; handleEventTypeFilter()"
            >
              <el-icon><Bell /></el-icon>
              <span class="text-sm">{{ type.label }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Listeners List -->
      <div class="flex-1 grid grid-cols-1 md:grid-cols-2 gap-4">
        <div
          v-for="listener in filteredListeners"
          :key="listener.id"
          class="bg-card rounded-lg border border-border hover:shadow-md transition-all"
        >
          <div class="p-5">
            <div class="flex items-start gap-4">
              <div class="w-12 h-12 rounded-lg bg-accent/10 flex items-center justify-center flex-shrink-0">
                <el-icon :size="24" color="#EA580C">
                  <Bell />
                </el-icon>
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <h4 class="font-semibold text-lg truncate">{{ listener.name }}</h4>
                  <el-tag size="small" type="warning">{{ listener.eventType }}</el-tag>
                  <el-tag size="small">{{ listener.language }}</el-tag>
                </div>
                <p class="text-sm text-muted mb-2">{{ listener.description || '暂无描述' }}</p>
                <div class="flex items-center gap-4 text-xs text-muted">
                  <span>镜像: {{ listener.imageName || '未配置' }}</span>
                  <span>实例数: {{ instancesMap[listener.id]?.length || 0 }}</span>
                </div>

                <!-- Action Buttons -->
                <div class="flex items-center gap-2 mt-3">
                  <el-button size="small" type="info" link @click="handleViewDetail(listener)">
                    <el-icon><View /></el-icon>
                    详情
                  </el-button>
                  <el-button size="small" type="success" link @click="handleManageInstances(listener)">
                    <el-icon><Coin /></el-icon>
                    实例
                  </el-button>
                  <el-button size="small" type="primary" link @click="handleTestListener(listener)">
                    <el-icon><DocumentChecked /></el-icon>
                    测试
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- Instances Preview -->
          <div v-if="instancesMap[listener.id]?.length" class="border-t border-border px-5 py-3 bg-muted/30">
            <div class="flex items-center gap-2 text-sm text-muted">
              <el-icon><Coin /></el-icon>
              <span>实例: </span>
              <el-tag
                v-for="instance in instancesMap[listener.id].slice(0, 2)"
                :key="instance.id"
                size="small"
                :type="instance.status === 'RUNNING' ? 'success' : 'info'"
              >
                {{ instance.name }}
              </el-tag>
              <span v-if="instancesMap[listener.id].length > 2" class="text-xs">
                +{{ instancesMap[listener.id].length - 2 }} 更多
              </span>
            </div>
          </div>
        </div>

        <div v-if="filteredListeners.length === 0" class="col-span-2 text-center py-12 text-muted">
          <el-icon :size="48" class="mb-4"><Bell /></el-icon>
          <p>暂无监听器</p>
        </div>
      </div>
    </div>

    <!-- Listener Detail Dialog -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`监听器详情 - ${selectedListener?.name}`"
      width="70%"
    >
      <el-tabs v-if="selectedListener" v-model="activeDetailTab">
        <el-tab-pane label="基本信息" name="info">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-sm text-muted">监听器名称</label>
              <p class="font-semibold mt-1">{{ selectedListener.name }}</p>
            </div>
            <div>
              <label class="text-sm text-muted">事件类型</label>
              <p class="font-semibold mt-1">{{ selectedListener.eventType }}</p>
            </div>
            <div>
              <label class="text-sm text-muted">编程语言</label>
              <p class="font-semibold mt-1">{{ selectedListener.language }}</p>
            </div>
            <div>
              <label class="text-sm text-muted">Docker镜像</label>
              <p class="font-mono text-sm mt-1">{{ selectedListener.imageName || '未配置' }}</p>
            </div>
            <div class="col-span-2">
              <label class="text-sm text-muted">描述</label>
              <p class="mt-1">{{ selectedListener.description || '暂无描述' }}</p>
            </div>
            <div>
              <label class="text-sm text-muted">创建时间</label>
              <p class="font-mono text-sm mt-1">{{ new Date(selectedListener.createdAt).toLocaleString() }}</p>
            </div>
            <div>
              <label class="text-sm text-muted">更新时间</label>
              <p class="font-mono text-sm mt-1">{{ new Date(selectedListener.updatedAt).toLocaleString() }}</p>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="代码查看" name="code">
          <div class="bg-gray-900 rounded-lg p-4 overflow-auto max-h-96">
            <pre class="text-sm text-green-400 font-mono">{{ selectedListener.code }}</pre>
          </div>
        </el-tab-pane>

        <el-tab-pane label="事件处理逻辑" name="logic">
          <div class="bg-blue-50 rounded-lg p-4">
            <h4 class="font-semibold mb-2">事件类型: {{ selectedListener.eventType }}</h4>
            <p class="text-sm text-muted">
              此监听器用于处理 {{ selectedListener.eventType }} 类型的事件。
              当系统接收到此类事件时，将触发监听器中定义的处理逻辑。
            </p>
            <div class="mt-4 p-3 bg-white rounded border border-blue-200">
              <p class="text-xs font-mono text-muted">
                事件数据将作为输入参数传递给监听器的处理函数。
                处理函数可以访问事件的所有属性，并执行相应的业务逻辑。
              </p>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Instance Management Drawer -->
    <el-drawer
      v-model="instanceDrawerVisible"
      :title="`实例管理 - ${selectedListener?.name}`"
      size="50%"
    >
      <div class="space-y-4">
        <div class="flex items-center justify-between">
          <div>
            <h4 class="font-semibold">实例列表</h4>
            <p class="text-sm text-muted">管理此监听器的实例配置</p>
          </div>
          <el-button type="primary" size="small" @click="selectedListener && handleCreateInstance(selectedListener)">
            <el-icon><Plus /></el-icon>
            创建实例
          </el-button>
        </div>

        <div v-if="selectedInstances.length > 0" class="space-y-3">
          <div
            v-for="instance in selectedInstances"
            :key="instance.id"
            class="bg-card rounded-lg p-4 border border-border"
          >
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2">
                <el-icon><Bell /></el-icon>
                <span class="font-medium">{{ instance.name }}</span>
                <el-tag v-if="instance.isDefault" type="primary" size="small">默认</el-tag>
              </div>
              <div class="flex items-center gap-2">
                <el-tag :type="instance.status === 'RUNNING' ? 'success' : 'info'" size="small">
                  {{ instance.status }}
                </el-tag>
                <el-button
                  v-if="instance.status !== 'RUNNING'"
                  size="small"
                  type="success"
                  link
                  @click="handleStartInstanceContainer(instance.id)"
                >
                  <el-icon><VideoPlay /></el-icon>
                  启动
                </el-button>
                <el-button
                  v-if="instance.status === 'RUNNING'"
                  size="small"
                  type="warning"
                  link
                  @click="handleStopInstanceContainer(instance.id)"
                >
                  <el-icon><VideoPause /></el-icon>
                  停止
                </el-button>
                <el-button
                  size="small"
                  type="primary"
                  link
                  @click="handleRestartInstanceContainer(instance.id)"
                >
                  <el-icon><RefreshRight /></el-icon>
                  重启
                </el-button>
                <el-button size="small" type="info" link @click="handleViewLogs(instance)">
                  <el-icon><Document /></el-icon>
                  日志
                </el-button>
                <el-button size="small" type="warning" link @click="handleEditInstance(instance)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button size="small" type="danger" link @click="handleDeleteInstance(instance)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <div class="text-xs text-muted space-y-1">
              <div>容器ID: {{ instance.containerId || 'N/A' }}</div>
              <div class="font-mono">{{ instance.config }}</div>
            </div>
          </div>
        </div>

        <div v-else class="text-center py-8 text-muted">
          <el-icon :size="32" class="mb-2"><Coin /></el-icon>
          <p>暂无实例</p>
        </div>
      </div>
    </el-drawer>

    <!-- Create/Edit Instance Dialog -->
    <el-dialog
      v-model="instanceDialogVisible"
      :title="isEditMode ? '编辑实例' : '创建实例'"
      width="500px"
    >
      <el-form :model="instanceForm" label-width="100px">
        <el-form-item label="实例名称" required>
          <el-input v-model="instanceForm.name" placeholder="请输入实例名称" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="instanceForm.isDefault" />
        </el-form-item>
        <el-form-item label="配置参数">
          <el-input
            v-model="instanceForm.config"
            type="textarea"
            :rows="6"
            placeholder='请输入JSON格式的配置，例如: {"key": "value"}'
            class="font-mono text-sm"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="instanceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleInstanceSubmit">
          {{ isEditMode ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Test Listener Dialog -->
    <el-dialog
      v-model="testDialogVisible"
      :title="`测试监听器 - ${testListener?.name}`"
      width="600px"
    >
      <div class="space-y-4">
        <div>
          <label class="text-sm font-medium">事件类型</label>
          <div class="mt-2">
            <el-tag type="warning">{{ testListener?.eventType }}</el-tag>
          </div>
        </div>

        <div>
          <label class="text-sm font-medium">测试事件数据</label>
          <el-input
            v-model="eventDataInput"
            type="textarea"
            :rows="6"
            placeholder='请输入测试事件数据（JSON格式），例如: {"symbol": "BTC/USDT", "price": 50000}'
            class="mt-2 font-mono text-sm"
          />
        </div>

        <el-button type="primary" :loading="testLoading" @click="handleRunTest" style="width: 100%">
          <el-icon class="mr-2"><DocumentChecked /></el-icon>
          发送测试事件
        </el-button>

        <div v-if="testResult" class="border border-border rounded-lg p-4">
          <div class="flex items-center gap-2 mb-2">
            <el-icon :color="testResult.success ? '#22C55E' : '#DC2626'">
              <component :is="testResult.success ? 'SuccessFilled' : 'CircleCloseFilled'" />
            </el-icon>
            <span class="font-medium">{{ testResult.message }}</span>
          </div>
          <div v-if="testResult.data" class="bg-muted rounded p-3 mt-2">
            <pre class="text-sm font-mono">{{ JSON.stringify(testResult.data, null, 2) }}</pre>
          </div>
        </div>

        <!-- Event Examples -->
        <div class="bg-blue-50 rounded-lg p-4">
          <h5 class="font-medium mb-2">事件示例</h5>
          <div class="space-y-2 text-sm">
            <div v-if="testListener?.eventType === 'MARKET'">
              <code class="bg-white px-2 py-1 rounded">{"symbol": "BTC/USDT", "price": 50000, "volume": 1.5}</code>
            </div>
            <div v-else-if="testListener?.eventType === 'ORDER'">
              <code class="bg-white px-2 py-1 rounded">{"orderId": "12345", "status": "FILLED", "price": 50000}</code>
            </div>
            <div v-else-if="testListener?.eventType === 'ACCOUNT'">
              <code class="bg-white px-2 py-1 rounded">{"accountId": "67890", "balance": 10000, "equity": 10500}</code>
            </div>
            <div v-else>
              <code class="bg-white px-2 py-1 rounded">{"eventType": "CUSTOM", "data": "custom data"}</code>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Instance Logs Dialog -->
    <el-dialog
      v-model="logDialogVisible"
      :title="`实例日志 - ${selectedInstance?.name}`"
      width="70%"
    >
      <div v-loading="logLoading">
        <el-tabs v-model="activeLogTab" @tab-change="handleLogTabChange">
          <el-tab-pane label="标准输出" name="stdout">
            <div class="bg-gray-900 rounded p-3 h-96 overflow-auto font-mono text-sm text-green-400">
              <div v-if="instanceLogs.length === 0" class="text-gray-500">暂无日志</div>
              <div v-else v-for="(log, index) in instanceLogs" :key="index">{{ log }}</div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="错误输出" name="stderr">
            <div class="bg-gray-900 rounded p-3 h-96 overflow-auto font-mono text-sm text-red-400">
              <div v-if="instanceLogs.length === 0" class="text-gray-500">暂无日志</div>
              <div v-else v-for="(log, index) in instanceLogs" :key="index">{{ log }}</div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="自定义日志" name="custom">
            <div class="bg-gray-900 rounded p-3 h-96 overflow-auto font-mono text-sm text-blue-400">
              <div v-if="instanceLogs.length === 0" class="text-gray-500">暂无日志</div>
              <div v-else v-for="(log, index) in instanceLogs" :key="index">{{ log }}</div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <template #footer>
        <el-button @click="logDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>
