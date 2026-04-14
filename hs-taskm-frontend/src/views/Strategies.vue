<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { strategyApi } from '@/api'
import type { Strategy } from '@/types'

const loading = ref(true)
const strategies = ref<Strategy[]>([])
const searchText = ref('')

// 详情对话框
const detailDialogVisible = ref(false)
const selectedStrategy = ref<Strategy | null>(null)
const activeDetailTab = ref('info')

// 参数配置器
const parameterForm = ref<Record<string, any>>({})
const parameterConfig = ref<any[]>([])

const filteredStrategies = computed(() => {
  if (!searchText.value) return strategies.value
  const search = searchText.value.toLowerCase()
  return strategies.value.filter(s =>
    s.name.toLowerCase().includes(search) ||
    (s.description && s.description.toLowerCase().includes(search))
  )
})

const loadStrategies = async () => {
  try {
    loading.value = true
    const res = await strategyApi.getStrategies()
    strategies.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('Failed to load strategies:', error)
    strategies.value = []
    ElMessage.error('加载策略失败')
  } finally {
    loading.value = false
  }
}

const handleViewDetail = (strategy: Strategy) => {
  selectedStrategy.value = strategy
  detailDialogVisible.value = true
  activeDetailTab.value = 'info'

  // 解析参数配置
  try {
    if (strategy.configParameters) {
      parameterConfig.value = JSON.parse(strategy.configParameters)
    }
    if (strategy.parameterDefaults) {
      parameterForm.value = JSON.parse(strategy.parameterDefaults)
    }
  } catch (error) {
    console.error('Failed to parse parameters:', error)
  }
}

const renderParameterInput = (config: any) => {
  const type = config.type || 'string'
  const value = parameterForm.value[config.name]

  if (type === 'string') {
    return value || ''
  } else if (type === 'number') {
    return value?.toString() || ''
  } else if (type === 'boolean') {
    return value ? 'true' : 'false'
  } else if (type === 'enum') {
    const option = config.options?.find((opt: any) => opt.value === value)
    return option?.label || ''
  }
  return ''
}

onMounted(() => {
  loadStrategies()
})
</script>

<template>
  <div v-loading="loading" class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h3 class="text-2xl font-bold">策略管理</h3>
        <p class="text-muted mt-1">查看和管理执行策略</p>
      </div>
      <el-input
        v-model="searchText"
        placeholder="搜索策略名称或描述"
        prefix-icon="Search"
        clearable
        style="width: 300px"
      />
    </div>

    <!-- Strategies List -->
    <div>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div
            v-for="strategy in filteredStrategies"
            :key="strategy.id"
            class="bg-card rounded-lg border border-border hover:shadow-lg transition-all p-5 group"
          >
            <div class="flex items-start justify-between mb-3">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                  <el-icon :size="20" color="#2563EB">
                    <Document />
                  </el-icon>
                </div>
                <div>
                  <h4 class="font-semibold text-foreground">{{ strategy.name }}</h4>
                  <el-tag size="small" type="info">{{ strategy.language }}</el-tag>
                </div>
              </div>
              <div class="opacity-0 group-hover:opacity-100 transition-opacity flex items-center gap-1">
                <el-button size="small" type="primary" link @click="handleViewDetail(strategy)">
                  <el-icon><View /></el-icon>
                </el-button>
              </div>
            </div>

            <p class="text-sm text-muted mb-3 line-clamp-2">
              {{ strategy.description || '暂无描述' }}
            </p>

            <div class="flex items-center justify-between text-xs text-muted">
              <div class="flex items-center gap-1">
                <el-icon><Clock /></el-icon>
                {{ new Date(strategy.createdAt).toLocaleString() }}
              </div>
              <el-button size="small" type="primary" link @click="handleViewDetail(strategy)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="filteredStrategies.length === 0" class="text-center py-12 text-muted">
          <el-icon :size="48" class="mb-4"><Document /></el-icon>
          <p>{{ searchText ? '未找到匹配的策略' : '暂无策略' }}</p>
        </div>
    </div>

    <!-- Strategy Detail Dialog -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`策略详情 - ${selectedStrategy?.name}`"
      width="80%"
    >
      <el-tabs v-if="selectedStrategy" v-model="activeDetailTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <div class="space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="text-sm text-muted">策略名称</label>
                <p class="font-semibold mt-1">{{ selectedStrategy.name }}</p>
              </div>
              <div>
                <label class="text-sm text-muted">编程语言</label>
                <p class="font-semibold mt-1">{{ selectedStrategy.language }}</p>
              </div>
              <div class="col-span-2">
                <label class="text-sm text-muted">描述</label>
                <p class="mt-1">{{ selectedStrategy.description || '暂无描述' }}</p>
              </div>
              <div>
                <label class="text-sm text-muted">Docker镜像</label>
                <p class="font-mono text-sm mt-1">{{ selectedStrategy.dockerImageId || '未配置' }}</p>
              </div>
              <div>
                <label class="text-sm text-muted">创建时间</label>
                <p class="font-mono text-sm mt-1">{{ new Date(selectedStrategy.createdAt).toLocaleString() }}</p>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 代码查看 -->
        <el-tab-pane label="代码查看" name="code">
          <div class="bg-gray-900 rounded-lg p-4 overflow-auto max-h-96">
            <pre class="text-sm text-green-400 font-mono">{{ selectedStrategy.code }}</pre>
          </div>
        </el-tab-pane>

        <!-- 参数配置 -->
        <el-tab-pane label="参数配置" name="parameters">
          <div v-if="parameterConfig.length > 0" class="space-y-4">
            <div v-for="config in parameterConfig" :key="config.name" class="bg-card rounded-lg p-4 border border-border">
              <div class="flex items-center justify-between mb-2">
                <label class="font-semibold">{{ config.label || config.name }}</label>
                <el-tag size="small" type="info">{{ config.type }}</el-tag>
              </div>
              <p v-if="config.description" class="text-sm text-muted mb-2">{{ config.description }}</p>
              <div class="bg-muted rounded p-2 font-mono text-sm">
                {{ renderParameterInput(config) }}
              </div>
            </div>
          </div>
          <div v-else class="text-center py-8 text-muted">
            暂无参数配置
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
