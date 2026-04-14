<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { taskApi } from '@/api'
import type { TaskStatistics, Task } from '@/types'
import { TaskStatus } from '@/types'

const loading = ref(true)
const statistics = ref<TaskStatistics>({
  CREATED: 0,
  RUNNING: 0,
  STOPPED: 0,
  COMPLETED: 0,
  FAILED: 0,
})

const tasks = ref<Task[]>([])

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
      taskApi.getTasks({ page: 0, size: 10, sort: 'createdAt' }),
    ])
    statistics.value = statsRes.data
    tasks.value = tasksRes.data
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  } finally {
    loading.value = false
  }
}

const handleStartTask = async (task: Task) => {
  try {
    await taskApi.startTask(task.id)
    ElMessage.success('任务启动成功')
    await loadData()
  } catch (error) {
    console.error('Failed to start task:', error)
  }
}

const handleStopTask = async (task: Task) => {
  try {
    await taskApi.stopTask(task.id)
    ElMessage.success('任务停止成功')
    await loadData()
  } catch (error) {
    console.error('Failed to stop task:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div v-loading="loading" class="space-y-6">
    <!-- KPI Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
      <div
        v-for="(count, status) in statistics"
        :key="status"
        class="bg-card rounded-lg p-6 border border-border hover:shadow-md transition-shadow"
      >
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-muted mb-1">{{ statusLabels[status as TaskStatus] }}</p>
            <p class="text-3xl font-bold" :style="{ color: statusColors[status as TaskStatus] }">
              {{ count }}
            </p>
          </div>
          <div class="w-12 h-12 rounded-full flex items-center justify-center"
               :style="{ backgroundColor: `${statusColors[status as TaskStatus]}20` }">
            <el-icon :size="24" :color="statusColors[status as TaskStatus]">
              <component :is="status === TaskStatus.RUNNING ? 'VideoPlay' : 'VideoPause'" />
            </el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- Task List -->
    <div class="bg-card rounded-lg border border-border">
      <div class="p-6 border-b border-border">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-semibold">最近任务</h3>
        </div>
      </div>

      <el-table :data="tasks" class="w-full">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="strategyId" label="策略ID" width="100" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :color="statusColors[row.status]">
              {{ statusLabels[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
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
              @click="$router.push(`/tasks/${row.id}`)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
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
