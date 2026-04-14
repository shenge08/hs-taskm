<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const activeMenu = computed(() => route.path)

const menuItems = [
  { path: '/tasks', icon: 'List', title: '任务管理' },
  { path: '/strategies', icon: 'Document', title: '策略管理' },
  { path: '/plugins', icon: 'Connection', title: '插件管理' },
  { path: '/listeners', icon: 'Bell', title: '监听器管理' },
]

const handleMenuSelect = (index: string) => {
  router.push(index)
}
</script>

<template>
  <div class="flex h-screen bg-background">
    <!-- Sidebar -->
    <aside class="w-64 bg-card border-r border-border flex flex-col">
      <!-- Logo -->
      <div class="h-16 flex items-center px-6 border-b border-border">
        <h1 class="font-heading text-xl font-bold text-primary">
          HS-TASKM
        </h1>
      </div>

      <!-- Navigation -->
      <nav class="flex-1 px-4 py-6">
        <el-menu
          :default-active="activeMenu"
          @select="handleMenuSelect"
          background-color="transparent"
          text-color="#64748B"
          active-text-color="#2563EB"
          class="border-none"
        >
          <el-menu-item
            v-for="item in menuItems"
            :key="item.path"
            :index="item.path"
            class="mb-2 rounded-lg hover:bg-muted"
          >
            <template #title>
              <div class="flex items-center gap-3">
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.title }}</span>
              </div>
            </template>
          </el-menu-item>
        </el-menu>
      </nav>

      <!-- Footer -->
      <div class="p-4 border-t border-border">
        <div class="text-xs text-muted text-center">
          多语言策略容器执行平台
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 flex flex-col overflow-hidden">
      <!-- Header -->
      <header class="h-16 bg-card border-b border-border flex items-center px-6">
        <div class="flex-1">
          <h2 class="text-lg font-semibold">{{ route.meta.title || '首页' }}</h2>
        </div>
        <div class="flex items-center gap-4">
          <el-button circle>
            <el-icon><Setting /></el-icon>
          </el-button>
          <el-button circle>
            <el-icon><User /></el-icon>
          </el-button>
        </div>
      </header>

      <!-- Content -->
      <div class="flex-1 overflow-auto p-6">
        <router-view />
      </div>
    </main>
  </div>
</template>

<style scoped>
:deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
}

:deep(.el-menu-item.is-active) {
  background-color: #F8FAFC;
  color: #2563EB;
  font-weight: 500;
}
</style>
