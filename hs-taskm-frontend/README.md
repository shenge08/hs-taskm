# HS-TASKM Frontend

多语言策略容器执行平台的前端管理界面

## 🚀 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问 http://localhost:3000
```

## 📦 项目结构

```
src/
├── api/              # API 客户端
│   ├── client.ts     # Axios 配置
│   ├── modules/      # API 模块
│   │   ├── task.ts
│   │   ├── strategy.ts
│   │   ├── plugin.ts
│   │   └── listener.ts
│   └── index.ts
├── components/       # 组件
│   └── Layout.vue    # 主布局
├── views/            # 页面
│   ├── Dashboard.vue
│   ├── Strategies.vue
│   ├── Plugins.vue
│   ├── Listeners.vue
│   └── TaskDetail.vue
├── router/           # 路由
├── types/            # 类型定义
├── App.vue
├── main.ts
└── style.css
```

## 🎨 技术栈

- Vue 3 + TypeScript
- Vite
- Element Plus
- Tailwind CSS
- Pinia
- Vue Router
- Axios

## 📱 功能模块

- **仪表盘** - 任务统计和监控
- **策略管理** - 管理交易策略
- **插件管理** - 管理数据插件
- **监听器管理** - 管理事件监听器
- **任务详情** - 任务监控和日志
