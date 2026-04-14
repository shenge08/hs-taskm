# HS-TASKM 前端设计文档

基于 **ui-ux-pro-max** skill 的设计建议

## 🎨 设计系统

### Dashboard 风格
**选择**: Data-Dense Dashboard（数据密集型仪表盘）
- 原因：需要展示多个任务、策略、插件的状态
- 特点：高效网格布局、紧凑间距、最大信息密度

### 色彩方案
**选择**: SaaS (General) - 信任蓝 + 橙色 CTA

```css
/* 主色调 */
--primary: #2563EB;          /* 蓝色 - 主要操作 */
--on-primary: #FFFFFF;        /* 主色文字 */
--secondary: #3B82F6;        /* 次要蓝色 */
--accent: #EA580C;           /* 橙色 - 强调/CTA */
--success: #22C55E;          /* 绿色 - 成功/运行 */
--warning: #F59E0B;          /* 黄色 - 警告 */
--danger: #DC2626;           /* 红色 - 危险/停止 */
--background: #F8FAFC;       /* 背景色 */
--foreground: #1E293B;       /* 前景色 */
--card: #FFFFFF;             /* 卡片背景 */
--border: #E2E8F0;           /* 边框色 */
--muted: #64748B;            /* 弱化文字 */
```

### 字体系统
**选择**: Fira Code + Fira Sans

```css
/* 标题字体 */
--font-heading: 'Fira Code', monospace;

/* 正文字体 */
--font-body: 'Fira Sans', sans-serif;

/* 字号 */
--text-xs: 12px;
--text-sm: 14px;
--text-base: 16px;
--text-lg: 18px;
--text-xl: 20px;
--text-2xl: 24px;
--text-3xl: 30px;
```

### 布局系统
```css
/* 间距 - 8px 基准 */
--spacing-1: 4px;
--spacing-2: 8px;
--spacing-3: 12px;
--spacing-4: 16px;
--spacing-5: 20px;
--spacing-6: 24px;
--spacing-8: 32px;

/* 网格 */
--grid-columns: 12;
--grid-gap: 8px;
--card-padding: 12px;

/* 断点 */
--breakpoint-sm: 640px;
--breakpoint-md: 768px;
--breakpoint-lg: 1024px;
--breakpoint-xl: 1280px;
```

## 📱 页面结构

### 主要页面

1. **Dashboard（仪表盘）**
   - KPI 卡片行（任务总数、运行中、已完成、失败）
   - 任务列表表格（分页、排序、筛选）
   - 实时监控图表（CPU、内存）

2. **Strategies（策略管理）**
   - 策略列表
   - 创建/编辑策略表单
   - 代码编辑器（Monaco Editor）

3. **Plugins（插件管理）**
   - 插件列表
   - 插件实例管理
   - 容器控制（启动/停止）

4. **Listeners（监听器管理）**
   - 监听器列表
   - 监听器实例管理
   - 容器控制

5. **Tasks（任务详情）**
   - 任务详情卡片
   - 容器状态
   - 资源监控图表
   - 日志查看器

## 🧩 组件设计

### 通用组件

1. **StatusBadge（状态徽章）**
   - CREATED: 灰色
   - RUNNING: 绿色
   - STOPPED: 黄色
   - COMPLETED: 蓝色
   - FAILED: 红色

2. **ActionButton（操作按钮）**
   - 主按钮：primary 蓝色
   - 次按钮：secondary
   - 危险按钮：danger 红色
   - 图标 + 文字

3. **DataTable（数据表格）**
   - 排序
   - 分页
   - 筛选
   - 行选择

4. **MetricCard（指标卡片）**
   - 标题
   - 数值（大字号）
   - 趋势指示器（↑↓）
   - 图标

## 🎭 交互模式

### 加载状态
- Skeleton screens（骨架屏）
- Loading spinners
- 进度条

### 错误处理
- Toast 通知
- 错误边界
- 表单验证

### 响应式
- 移动优先
- 触摸目标最小 44×44px
- 横向滚动禁用

## ♿ 可访问性

- WCAG AA 标准
- 键盘导航
- 焦点指示器
- ARIA 标签
- 颜色对比度 4.5:1

## 🎨 UI 风格

### 卡片设计
- 白色背景
- 微妙阴影
- 圆角 8px
- 内边距 12px

### 按钮设计
- 高度 40px
- 圆角 6px
- 图标 + 文字间距 8px
- Hover 状态变化

### 表单设计
- 标签在字段上方
- 错误信息靠近字段
- 帮助文本灰色
- 必填标记*

## 📦 技术栈

- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **UI 库**: Element Plus
- **图表库**: ECharts
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP 客户端**: Axios
- **代码编辑器**: Monaco Editor
- **CSS 框架**: Tailwind CSS
