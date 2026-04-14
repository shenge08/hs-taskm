<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { taskApi } from '@/api'
import type { Task, ContainerMetrics } from '@/types'
import { TaskStatus } from '@/types'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const task = ref<Task | null>(null)
const metrics = ref<ContainerMetrics | null>(null)

const activeTab = ref('overview')

const statusColors: Record<TaskStatus, string> = {
  [TaskStatus.CREATED]: '#64748B',
  [TaskStatus.RUNNING]: '#22C55E',
  [TaskStatus.STOPPED]: '#F59E0B',
  [TaskStatus.COMPLETED]: '#2563EB',
  [TaskStatus.FAILED]: '#DC2626',
}

const canStart = computed(() => {
  return task.value?.status === TaskStatus.CREATED || task.value?.status === TaskStatus.STOPPED
})

const canStop = computed(() => {
  return task.value?.status === TaskStatus.RUNNING
})

const loadTask = async () => {
  try {
    loading.value = true
    const taskId = Number(route.params.id)
    const [taskRes, metricsRes] = await Promise.all([
      taskApi.getTask(taskId),
      taskApi.getTaskMetrics(taskId),
    ])
    task.value = taskRes.data
    metrics.value = metricsRes.data
  } catch (error) {
    console.error('Failed to load task:', error)
  } finally {
    loading.value = false
  }
}

const handleStart = async () => {
  if (!task.value) return
  try {
    await taskApi.startTask(task.value.id)
    ElMessage.success('任务启动成功')
    await loadTask()
  } catch (error) {
    console.error('Failed to start task:', error)
  }
}

const handleStop = async () => {
  if (!task.value) return
  try {
    await taskApi.stopTask(task.value.id)
    ElMessage.success('任务停止成功')
    await loadTask()
  } catch (error) {
    console.error('Failed to stop task:', error)
  }
}

onMounted(() => {
  loadTask()
})
</script>

<template>
  <div v-loading="loading" class="space-y-6">
    <!-- Header -->
    <div class="bg-card rounded-lg border border-border p-6">
      <div class="flex items-center justify-between mb-4">
        <div class="flex items-center gap-4">
          <el-button circle @click="router.back()">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <div>
            <h3 class="text-2xl font-bold">任务详情</h3>
            <p class="text-muted text-sm">任务ID: {{ task?.id }}</p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <el-button
            v-if="canStart"
            type="success"
            @click="handleStart"
          >
            <el-icon><VideoPlay /></el-icon>
            启动任务
          </el-button>
          <el-button
            v-if="canStop"
            type="warning"
            @click="handleStop"
          >
            <el-icon><VideoPause /></el-icon>
            停止任务
          </el-button>
        </div>
      </div>

      <!-- Task Info -->
      <div v-if="task" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div>
          <div class="text-sm text-muted mb-1">状态</div>
          <div class="flex items-center gap-2">
            <div
              class="w-3 h-3 rounded-full"
              :style="{ backgroundColor: statusColors[task.status] }"
            />
            <span class="font-medium">{{ task.status }}</span>
          </div>
        </div>
        <div>
          <div class="text-sm text-muted mb-1">策略ID</div>
          <div class="font-medium">{{ task.strategyId }}</div>
        </div>
        <div>
          <div class="text-sm text-muted mb-1">创建时间</div>
          <div class="font-medium">{{ new Date(task.createdAt).toLocaleString() }}</div>
        </div>
        <div>
          <div class="text-sm text-muted mb-1">启动时间</div>
          <div class="font-medium">
            {{ task.startedAt ? new Date(task.startedAt).toLocaleString() : 'N/A' }}
          </div>
        </div>
      </div>
    </div>

    <!-- Metrics -->
    <div v-if="metrics" class="bg-card rounded-lg border border-border p-6">
      <h4 class="text-lg font-semibold mb-4">资源监控</h4>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div>
          <div class="text-sm text-muted mb-2">CPU使用率</div>
          <el-progress
            :percentage="Number(metrics.cpuPercent.toFixed(2))"
            :color="'#2563EB'"
          />
        </div>
        <div>
          <div class="text-sm text-muted mb-2">内存使用率</div>
          <el-progress
            :percentage="Number(metrics.memoryPercent.toFixed(2))"
            :color="'#22C55E'"
          />
        </div>
        <div>
          <div class="text-sm text-muted mb-2">内存使用</div>
          <div class="text-lg font-semibold">
            {{ (metrics.memoryUsage / 1024 / 1024).toFixed(2) }} MB /
            {{ (metrics.memoryLimit / 1024 / 1024).toFixed(2) }} MB
          </div>
        </div>
        <div>
          <div class="text-sm text-muted mb-2">网络I/O</div>
          <div class="text-lg font-semibold">
            ↓ {{ (metrics.networkRx / 1024).toFixed(2) }} KB/s
            ↑ {{ (metrics.networkTx / 1024).toFixed(2) }} KB/s
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
