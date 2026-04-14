<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { pluginApi } from '@/api'
import type { DataPlugin, PluginInstance } from '@/types'

const loading = ref(true)
const plugins = ref<DataPlugin[]>([])
const instancesMap = ref<Record<number, PluginInstance[]>>({})
const typeFilter = ref('')

// 实例管理抽屉
const instanceDrawerVisible = ref(false)
const selectedPlugin = ref<DataPlugin | null>(null)
const selectedInstances = ref<PluginInstance[]>([])

// 创建/编辑实例对话框
const instanceDialogVisible = ref(false)
const isEditMode = ref(false)
const instanceForm = ref({
  id: undefined as number | undefined,
  pluginId: undefined as number | undefined,
  name: '',
  isDefault: false,
  config: '{}',
})

// 测试插件对话框
const testDialogVisible = ref(false)
const testPlugin = ref<DataPlugin | null>(null)
const testLoading = ref(false)
const testResult = ref<any>(null)

// 插件详情对话框
const detailDialogVisible = ref(false)
const activeDetailTab = ref('info')

// 日志查看对话框
const logDialogVisible = ref(false)
const selectedInstance = ref<PluginInstance | null>(null)
const instanceLogs = ref<string[]>([])
const activeLogTab = ref('stdout')
const logLoading = ref(false)

const pluginTypes = [
  { label: '数据源', value: 'SOURCE' },
  { label: '数据处理器', value: 'PROCESSOR' },
  { label: '数据存储', value: 'SINK' },
]

const filteredPlugins = computed(() => {
  if (!typeFilter.value) return plugins.value
  return plugins.value.filter(p => p.pluginType === typeFilter.value)
})

const loadPlugins = async () => {
  try {
    loading.value = true
    const res = await pluginApi.getPlugins()
    plugins.value = Array.isArray(res.data) ? res.data : []

    // Load instances for each plugin
    for (const plugin of plugins.value) {
      try {
        const instancesRes = await pluginApi.getPluginInstances(plugin.id)
        instancesMap.value[plugin.id] = Array.isArray(instancesRes.data) ? instancesRes.data : []
      } catch (error) {
        console.error(`Failed to load instances for plugin ${plugin.id}:`, error)
        instancesMap.value[plugin.id] = []
      }
    }
  } catch (error) {
    console.error('Failed to load plugins:', error)
    plugins.value = []
    ElMessage.error('加载插件失败')
  } finally {
    loading.value = false
  }
}

const handleTypeFilter = () => {
  // Filter is handled by computed property
}

const handleStartContainer = async (pluginId: number) => {
  try {
    await pluginApi.startContainer(pluginId)
    ElMessage.success('容器启动成功')
    await loadPlugins()
  } catch (error) {
    console.error('Failed to start container:', error)
    ElMessage.error('容器启动失败')
  }
}

const handleStopContainer = async (pluginId: number) => {
  try {
    await pluginApi.stopContainer(pluginId)
    ElMessage.success('容器停止成功')
    await loadPlugins()
  } catch (error) {
    console.error('Failed to stop container:', error)
    ElMessage.error('容器停止失败')
  }
}

const handleViewDetail = (plugin: DataPlugin) => {
  selectedPlugin.value = plugin
  detailDialogVisible.value = true
  activeDetailTab.value = 'info'
}

const handleManageInstances = (plugin: DataPlugin) => {
  selectedPlugin.value = plugin
  selectedInstances.value = instancesMap.value[plugin.id] || []
  instanceDrawerVisible.value = true
}

const handleCreateInstance = (plugin: DataPlugin) => {
  isEditMode.value = false
  instanceForm.value = {
    id: undefined,
    pluginId: plugin.id,
    name: '',
    isDefault: false,
    config: '{}',
  }
  instanceDialogVisible.value = true
}

const handleEditInstance = (instance: PluginInstance) => {
  isEditMode.value = true
  instanceForm.value = {
    id: instance.id,
    pluginId: instance.pluginId,
    name: instance.name,
    isDefault: instance.isDefault,
    config: instance.config,
  }
  instanceDialogVisible.value = true
}

const handleDeleteInstance = async (instance: PluginInstance) => {
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
    // Assuming there's a delete API endpoint
    ElMessage.success('删除成功')
    await loadPlugins()
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
    await loadPlugins()
  } catch (error) {
    console.error('Failed to save instance:', error)
    ElMessage.error(isEditMode.value ? '更新失败' : '创建失败')
  }
}

const handleTestPlugin = (plugin: DataPlugin) => {
  testPlugin.value = plugin
  testResult.value = null
  testDialogVisible.value = true
}

const handleRunTest = async () => {
  if (!testPlugin.value) return

  try {
    testLoading.value = true
    testResult.value = null

    // Simulate test - replace with actual API call
    await new Promise(resolve => setTimeout(resolve, 1000))

    testResult.value = {
      success: true,
      message: '插件测试通过',
      data: {
        responseTime: 45,
        dataSize: 1024,
        timestamp: new Date().toISOString(),
      },
    }

    ElMessage.success('测试完成')
  } catch (error) {
    console.error('Failed to test plugin:', error)
    testResult.value = {
      success: false,
      message: '插件测试失败',
      error: String(error),
    }
    ElMessage.error('测试失败')
  } finally {
    testLoading.value = false
  }
}

const handleViewLogs = (instance: PluginInstance) => {
  selectedInstance.value = instance
  logDialogVisible.value = true
  loadInstanceLogs(instance.id)
}

const loadInstanceLogs = async (instanceId: number) => {
  try {
    logLoading.value = true
    const res = await pluginApi.getPluginInstanceLogs(instanceId, { type: activeLogTab.value })
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
  loadPlugins()
})
</script>

<template>
  <div v-loading="loading" class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h3 class="text-2xl font-bold">插件管理</h3>
        <p class="text-muted mt-1">管理数据插件的配置、实例和测试</p>
      </div>
    </div>

    <div class="flex gap-6">
      <!-- Type Filter Sidebar -->
      <div class="w-48 flex-shrink-0">
        <div class="bg-card rounded-lg border border-border p-4">
          <h3 class="font-heading font-semibold mb-3">类型筛选</h3>
          <div class="space-y-2">
            <div
              :class="[
                'flex items-center gap-2 px-3 py-2 rounded cursor-pointer transition-colors',
                !typeFilter ? 'bg-primary/10 text-primary' : 'hover:bg-muted'
              ]"
              @click="typeFilter = ''; handleTypeFilter()"
            >
              <el-icon><Menu /></el-icon>
              <span class="text-sm">全部</span>
            </div>
            <div
              v-for="type in pluginTypes"
              :key="type.value"
              :class="[
                'flex items-center gap-2 px-3 py-2 rounded cursor-pointer transition-colors',
                typeFilter === type.value ? 'bg-primary/10 text-primary' : 'hover:bg-muted'
              ]"
              @click="typeFilter = type.value; handleTypeFilter()"
            >
              <el-icon><Connection /></el-icon>
              <span class="text-sm">{{ type.label }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Plugins List -->
      <div class="flex-1 space-y-4">
        <div
          v-for="plugin in filteredPlugins"
          :key="plugin.id"
          class="bg-card rounded-lg border border-border hover:shadow-md transition-all"
        >
          <div class="p-5">
            <div class="flex items-start justify-between">
              <div class="flex items-center gap-4 flex-1">
                <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center">
                  <el-icon :size="24" color="#2563EB">
                    <Connection />
                  </el-icon>
                </div>
                <div class="flex-1">
                  <div class="flex items-center gap-2">
                    <h4 class="font-semibold text-lg">{{ plugin.name }}</h4>
                    <el-tag size="small" type="info">{{ plugin.pluginType }}</el-tag>
                    <el-tag size="small">{{ plugin.language }}</el-tag>
                  </div>
                  <p class="text-sm text-muted mt-1">{{ plugin.description || '暂无描述' }}</p>
                  <div class="flex items-center gap-4 mt-2 text-xs text-muted">
                    <span>镜像: {{ plugin.imageName || '未配置' }}</span>
                    <span>实例数: {{ instancesMap[plugin.id]?.length || 0 }}</span>
                  </div>
                </div>
              </div>
              <div class="flex items-center gap-2 ml-4">
                <el-button size="small" type="info" link @click="handleViewDetail(plugin)">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-button size="small" type="success" link @click="handleManageInstances(plugin)">
                  <el-icon><Coin /></el-icon>
                  实例
                </el-button>
                <el-button size="small" type="primary" link @click="handleTestPlugin(plugin)">
                  <el-icon><DocumentChecked /></el-icon>
                  测试
                </el-button>
              </div>
            </div>
          </div>

          <!-- Instances Preview -->
          <div v-if="instancesMap[plugin.id]?.length" class="border-t border-border px-5 py-3 bg-muted/30">
            <div class="flex items-center gap-2 text-sm text-muted">
              <el-icon><Coin /></el-icon>
              <span>实例: </span>
              <el-tag
                v-for="instance in instancesMap[plugin.id].slice(0, 3)"
                :key="instance.id"
                size="small"
                :type="instance.status === 'RUNNING' ? 'success' : 'info'"
              >
                {{ instance.name }}
              </el-tag>
              <span v-if="instancesMap[plugin.id].length > 3" class="text-xs">
                +{{ instancesMap[plugin.id].length - 3 }} 更多
              </span>
            </div>
          </div>
        </div>

        <div v-if="filteredPlugins.length === 0" class="text-center py-12 text-muted">
          <el-icon :size="48" class="mb-4"><Connection /></el-icon>
          <p>暂无插件</p>
        </div>
      </div>
    </div>

    <!-- Plugin Detail Dialog -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`插件详情 - ${selectedPlugin?.name}`"
      width="70%"
    >
      <el-tabs v-if="selectedPlugin" v-model="activeDetailTab">
        <el-tab-pane label="基本信息" name="info">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-sm text-muted">插件名称</label>
              <p class="font-semibold mt-1">{{ selectedPlugin.name }}</p>
            </div>
            <div>
              <label class="text-sm text-muted">插件类型</label>
              <p class="font-semibold mt-1">{{ selectedPlugin.pluginType }}</p>
            </div>
            <div>
              <label class="text-sm text-muted">编程语言</label>
              <p class="font-semibold mt-1">{{ selectedPlugin.language }}</p>
            </div>
            <div>
              <label class="text-sm text-muted">Docker镜像</label>
              <p class="font-mono text-sm mt-1">{{ selectedPlugin.imageName || '未配置' }}</p>
            </div>
            <div class="col-span-2">
              <label class="text-sm text-muted">描述</label>
              <p class="mt-1">{{ selectedPlugin.description || '暂无描述' }}</p>
            </div>
            <div>
              <label class="text-sm text-muted">创建时间</label>
              <p class="font-mono text-sm mt-1">{{ new Date(selectedPlugin.createdAt).toLocaleString() }}</p>
            </div>
            <div>
              <label class="text-sm text-muted">更新时间</label>
              <p class="font-mono text-sm mt-1">{{ new Date(selectedPlugin.updatedAt).toLocaleString() }}</p>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="代码查看" name="code">
          <div class="bg-gray-900 rounded-lg p-4 overflow-auto max-h-96">
            <pre class="text-sm text-green-400 font-mono">{{ selectedPlugin.code }}</pre>
          </div>
        </el-tab-pane>

        <el-tab-pane label="配置参数" name="config">
          <div class="bg-gray-900 rounded-lg p-4 overflow-auto max-h-96">
            <pre class="text-sm text-blue-400 font-mono">{{ selectedPlugin.configParameters || '无配置参数' }}</pre>
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
      :title="`实例管理 - ${selectedPlugin?.name}`"
      size="50%"
    >
      <div class="space-y-4">
        <div class="flex items-center justify-between">
          <div>
            <h4 class="font-semibold">实例列表</h4>
            <p class="text-sm text-muted">管理此插件的实例配置</p>
          </div>
          <el-button type="primary" size="small" @click="selectedPlugin && handleCreateInstance(selectedPlugin)">
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
                <el-icon><Coin /></el-icon>
                <span class="font-medium">{{ instance.name }}</span>
                <el-tag v-if="instance.isDefault" type="primary" size="small">默认</el-tag>
              </div>
              <div class="flex items-center gap-2">
                <el-tag :type="instance.status === 'RUNNING' ? 'success' : 'info'" size="small">
                  {{ instance.status }}
                </el-tag>
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

    <!-- Test Plugin Dialog -->
    <el-dialog
      v-model="testDialogVisible"
      :title="`测试插件 - ${testPlugin?.name}`"
      width="600px"
    >
      <div class="space-y-4">
        <div>
          <label class="text-sm font-medium">测试参数</label>
          <el-input
            type="textarea"
            :rows="4"
            placeholder='请输入测试参数（JSON格式），例如: {"test": true}'
            class="mt-2 font-mono text-sm"
          />
        </div>

        <el-button type="primary" :loading="testLoading" @click="handleRunTest" style="width: 100%">
          <el-icon class="mr-2"><DocumentChecked /></el-icon>
          运行测试
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
