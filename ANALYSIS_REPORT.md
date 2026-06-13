# 自由画布（Free Canvas）— 分析报告

> 可视化剧情编辑器：将角色、场景、台词组合为 AI 驱动的影像创作工作台

---

## 一、项目概述

### 1.1 功能定位

自由画布是一个**节点式剧情编辑器**，用户可以通过拖拽和连接不同类型的节点（角色、场景、台词、视频），来组织和可视化故事结构。最终目标是将这些创意节点通过豆包大模型（Seedream 生图 + Seedance 2.0 生视频）自动转化为影像内容。

### 1.2 核心功能

| 功能 | 说明 |
|------|------|
| 节点管理 | 创建/编辑/删除/拖拽 4 类节点（角色、场景、台词、视频） |
| 连线关系 | 角色→视频、场景→视频、台词→视频 建立逻辑关联 |
| AI 生图 | 调用豆包 Seedream 3.0 模型，根据提示词生成图片 |
| AI 生视频 | 调用豆包 Seedance 2.0 模型，根据提示词生成视频 |
| 实时协作 | WebSocket 推送节点变更，多人同时编辑同一项目 |
| 项目持久化 | 所有节点和连接保存到 MySQL，支持多项目管理 |

---

## 二、技术架构

### 2.1 技术选型总览

```
┌─────────────────────────────────────────────────────────┐
│                       前端 (Vue 3)                        │
│  ┌──────────┬──────────┬──────────┬──────────────────┐  │
│  │ Vue Flow │ Pinia    │ Element  │ Axios + WebSocket│  │
│  │ 节点画布  │ 状态管理  │ Plus UI  │ 通信层            │  │
│  └──────────┴──────────┴──────────┴──────────────────┘  │
├─────────────────────────────────────────────────────────┤
│                     HTTP + WebSocket                      │
├─────────────────────────────────────────────────────────┤
│                   后端 (Spring Boot 3)                    │
│  ┌──────────┬──────────┬──────────┬──────────────────┐  │
│  │ REST API │ WebSocket│ @Async   │ HTTP Client      │  │
│  │ 控制器    │ 实时推送  │ 异步任务  │ AI 模型调用       │  │
│  └──────────┴──────────┴──────────┴──────────────────┘  │
│  ┌──────────┬──────────┐                                │
│  │ MyBatis  │ MySQL    │                                │
│  │ Plus     │ 数据存储  │                                │
│  └──────────┴──────────┘                                │
├─────────────────────────────────────────────────────────┤
│                    外部 AI 服务                            │
│  ┌─────────────────────┬──────────────────────────┐     │
│  │ 豆包 Seedream 3.0   │ 豆包 Seedance 2.0        │     │
│  │ (图片生成)           │ (视频生成)                │     │
│  └─────────────────────┴──────────────────────────┘     │
└─────────────────────────────────────────────────────────┘
```

### 2.2 技术选型理由

#### 前端

| 技术 | 版本 | 选型理由 |
|------|------|----------|
| **Vue 3** | 3.4+ | 主流框架，Composition API + TypeScript 支持良好 |
| **Vue Flow** | 1.39+ | Vue 生态最成熟的节点图库（React Flow 的 Vue 版），内置拖拽、缩放、小地图、连线 |
| **Pinia** | 2.1+ | Vue 3 官方状态管理，轻量且类型安全 |
| **Element Plus** | 2.5+ | 成熟的企业级 Vue 3 UI 组件库，表单/对话框/标签等开箱即用 |
| **Vite** | 5.0+ | 极快的开发服务器和构建工具 |

> **Vue Flow 选型特别说明**：自由画布的本质是「节点 + 连线」的有向图。Vue Flow 内置了节点拖拽、贝塞尔曲线连线、画布缩放平移、小地图导航等功能。自研画布需要至少 2000+ 行代码才能达到同等效果，使用 Vue Flow 可节省约 60% 的前端开发量。

#### 后端

| 技术 | 版本 | 选型理由 |
|------|------|----------|
| **Spring Boot** | 3.2 | Java 生态标准框架，约定优于配置 |
| **MyBatis-Plus** | 3.5 | 零 SQL 的 CRUD + JSON 字段支持（节点自定义数据使用 MySQL JSON 类型） |
| **MySQL** | 8.0 | JSON 字段完美适配节点灵活数据需求，无需引入 MongoDB |
| **WebSocket** | Spring 内置 | 轻量实现实时协作，避免引入额外 MQ 组件 |
| **@Async** | Spring 内置 | 异步执行 AI 生成任务，视频生成不阻塞 API 响应 |

#### 未引入的组件及理由

| 组件 | 未引入理由 |
|------|-----------|
| **Redis** | 当前场景无高频缓存需求；节点数据通过 WebSocket 实时同步即可；AI 任务状态直接查 MySQL |
| **RabbitMQ / Kafka** | AI 生成任务量不大时，Spring `@Async` + 线程池足够；未来日生成量 >10 万时可引入 |
| **MongoDB** | MySQL 8.0 的 JSON 字段已能满足节点动态数据存储需求 |
| **Nginx** | 开发阶段 Vite proxy 即可；生产环境部署时再引入 |

### 2.3 组件必要性分析

```
必须组件:
  ✅ Vue 3 + Vue Flow    — 画布核心，无可替代
  ✅ Spring Boot         — Java 后端，用户指定
  ✅ MySQL               — 数据持久化，用户指定
  ✅ WebSocket           — 实时协作 + AI 进度推送，轻量必要

可选组件（已引入，体量小）:
  ✅ Element Plus        — UI 组件库，提升开发效率
  ✅ Pinia               — 状态管理，Vue 3 官方推荐
  ✅ @Async 线程池       — 异步 AI 调用，Spring 内置

暂不引入:
  ❌ Redis               — 等数据量大时再加
  ❌ MQ                  — 等任务量需要时再加
  ❌ MongoDB             — MySQL JSON 已够用
```

---

## 三、数据模型设计

### 3.1 ER 图

```
┌─────────────┐    1:N    ┌───────────────┐    N:M    ┌──────────────┐
│   project   │──────────→│  canvas_node   │←─────────→│node_connection│
│             │           │                │           │              │
│ id          │           │ id             │           │ id           │
│ name        │           │ project_id(FK) │           │ project_id   │
│ description │           │ node_type      │           │ source_node  │
│ thumbnail   │           │ label          │           │ target_node  │
│ created_at  │           │ pos_x, pos_y   │           │ label        │
│ updated_at  │           │ width, height  │           │ style        │
└─────────────┘           │ node_data(JSON)│           └──────────────┘
                          │ style(JSON)    │
                          └───────────────┘
                                 │
                                 │ 1:N (关联视频节点的AI任务)
                                 │
                          ┌──────┴──────┐
                          │   ai_task    │
                          │ id           │
                          │ project_id   │
                          │ node_id(FK)  │
                          │ task_type    │ (IMAGE/VIDEO)
                          │ prompt       │
                          │ status       │ (PENDING/PROCESSING/COMPLETED/FAILED)
                          │ result_url   │
                          │ error_msg    │
                          └─────────────┘
```

### 3.2 节点类型数据模型

#### 角色节点 (CHARACTER)
```json
{
  "name": "关羽",
  "description": "身长九尺，髯长二尺，面如重枣",
  "age": 45,
  "gender": "男",
  "personality": "忠义、勇猛、骄傲",
  "avatarUrl": "",
  "voiceTone": "低沉威严"
}
```

#### 场景节点 (SCENE)
```json
{
  "name": "桃园",
  "description": "桃花盛开的园子",
  "location": "涿郡城外",
  "timeOfDay": "黄昏",
  "atmosphere": "庄严、热血",
  "backgroundUrl": "",
  "weather": "晴"
}
```

#### 台词节点 (TEXT)
```json
{
  "title": "桃园结义",
  "content": "念刘备、关羽、张飞，虽然异姓，既结为兄弟，则同心协力，救困扶危...",
  "speaker": "刘备",
  "emotion": "激昂",
  "fontSize": 16
}
```

#### 视频节点 (VIDEO)
```json
{
  "title": "桃园三结义",
  "description": "三人在桃园中焚香结拜",
  "prompt": "三个古代武将在一片桃花盛开的园子里，黄昏时分，庄严激昂地结拜",
  "videoUrl": "",
  "thumbnailUrl": "",
  "duration": 10,
  "status": "PENDING"
}
```

### 3.3 连线语义

| 连线方向 | 含义 |
|----------|------|
| 角色 → 视频 | 该角色出现在视频中 |
| 场景 → 视频 | 视频使用该场景作为背景 |
| 台词 → 视频 | 视频内容涵盖该段台词 |
| 角色 → 台词 | 该角色说这段台词（逻辑关联） |

---

## 四、API 设计

### 4.1 RESTful API

#### 项目管理

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/projects` | 获取所有项目 |
| `GET` | `/api/projects/{id}` | 获取项目详情 |
| `POST` | `/api/projects` | 创建项目 |
| `PUT` | `/api/projects/{id}` | 更新项目 |
| `DELETE` | `/api/projects/{id}` | 删除项目（级联删除节点和连接） |

#### 节点管理

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/projects/{id}/nodes` | 获取项目所有节点 |
| `POST` | `/api/projects/{id}/nodes` | 创建节点 |
| `PUT` | `/api/projects/{id}/nodes/{nodeId}` | 更新节点（位置/数据） |
| `DELETE` | `/api/projects/{id}/nodes/{nodeId}` | 删除节点 |

#### 连线管理

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/projects/{id}/connections` | 获取项目所有连线 |
| `POST` | `/api/projects/{id}/connections` | 创建连线 |
| `DELETE` | `/api/projects/{id}/connections/{connId}` | 删除连线 |

#### AI 生成

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/ai/generate-image` | 提交图片生成任务 (Seedream) |
| `POST` | `/api/ai/generate-video` | 提交视频生成任务 (Seedance) |
| `GET` | `/api/ai/task/{taskId}` | 查询任务状态 |
| `GET` | `/api/ai/health` | AI 服务健康检查 |

### 4.2 WebSocket 协议

**连接端点**: `ws://host/ws/canvas/{projectId}`

**消息格式**:
```json
{
  "type": "NODE_CREATED | NODE_UPDATED | NODE_DELETED | CONN_CREATED | CONN_DELETED | AI_TASK_UPDATE",
  "projectId": 1,
  "data": {},
  "taskId": 123,
  "nodeId": 456,
  "status": "COMPLETED",
  "resultUrl": "https://..."
}
```

**推送场景**:
- 节点/连线 CRUD → 实时同步到同项目的所有在线用户
- AI 生成进度变更 → 推送到项目频道，前端更新视频节点状态

---

## 五、AI 集成方案

### 5.1 豆包 Seedream (图片生成)

```
用户输入提示词
      │
      ▼
前端 AIPanel → POST /api/ai/generate-image
      │
      ▼
后端 @Async 异步线程
      │
      ▼
HTTP POST → https://ark.cn-beijing.volces.com/api/v3/images/generations
请求体: {
  "model": "doubao-seedream-3-0-t2i-250415",
  "prompt": "用户提示词 + 角色/场景描述拼接",
  "size": "1024x1024"
}
      │
      ▼
返回图片 URL → 更新 ai_task 表 → WebSocket 推送完成
```

### 5.2 豆包 Seedance 2.0 (视频生成)

```
用户输入提示词 + 拼接关联节点信息（角色+场景+台词）
      │
      ▼
前端 AIPanel → POST /api/ai/generate-video
      │
      ▼
后端 @Async 异步线程
      │
      ▼
HTTP POST → https://ark.cn-beijing.volces.com/api/v3/videos/generations
请求体: {
  "model": "doubao-seedance-2-0-t2v-250428",
  "prompt": "场景:桃园黄昏 + 角色:关羽+刘备+张飞 + 动作:焚香结拜",
  "duration": 10
}
      │
      ▼
返回视频 URL → 更新 ai_task 表 → WebSocket 推送完成
```

### 5.3 提示词智能拼接策略

视频生成时，系统可根据节点的连线关系，自动构建复合提示词：

```
基础模板:
  "场景: {scene.name}, {scene.description}, {scene.atmosphere}氛围"

角色追加（遍历所有连接到该视频的角色节点）:
  + " 角色: {character.name}, {character.gender}, {character.personality}"

台词追加:
  + " 内容: {text.content}"

风格后缀:
  + " 高质量, 电影级画质, 4K, 中文古装风格"
```

---

## 六、部署与运行

### 6.1 环境要求

| 组件 | 版本 | 用途 |
|------|------|------|
| JDK | 17+ | 后端运行环境 |
| Maven | 3.8+ | 后端构建 |
| MySQL | 8.0+ | 数据存储 |
| Node.js | 18+ | 前端构建 |
| npm | 9+ | 前端依赖管理 |

### 6.2 快速启动

```bash
# 1. 启动 MySQL，创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS free_canvas CHARACTER SET utf8mb4"

# 2. 启动后端
cd backend
mvn spring-boot:run
# 后端运行在 http://localhost:8080

# 3. 配置 AI API Key（环境变量或 application.yml）
export SEEDREAM_API_KEY="your-seedream-api-key"
export SEEDANCE_API_KEY="your-seedance-api-key"

# 4. 启动前端
cd frontend
npm install
npm run dev
# 前端运行在 http://localhost:3000
```

### 6.3 项目目录结构

```
free-canvas/
├── backend/                              # Spring Boot 后端
│   ├── pom.xml                           # Maven 配置
│   └── src/main/
│       ├── java/com/freecanvas/
│       │   ├── FreeCanvasApplication.java    # 启动类
│       │   ├── controller/                   # REST 控制器
│       │   │   ├── ProjectController.java    # 项目 API
│       │   │   ├── NodeController.java       # 节点/连线 API
│       │   │   └── AIGenerationController.java # AI 生成 API
│       │   ├── service/                      # 业务接口
│       │   │   └── impl/                     # 业务实现
│       │   │       ├── ProjectServiceImpl.java
│       │   │       ├── NodeServiceImpl.java
│       │   │       └── AIGenerationServiceImpl.java  # AI 调用核心
│       │   ├── model/
│       │   │   ├── entity/                   # 数据库实体
│       │   │   ├── dto/                      # 请求/响应 DTO
│       │   │   └── enums/                    # 枚举
│       │   ├── repository/                   # MyBatis Mapper
│       │   ├── config/                       # Spring 配置
│       │   └── websocket/                    # WebSocket 处理
│       └── resources/
│           ├── application.yml               # 应用配置
│           └── schema.sql                    # 数据库建表
│
├── frontend/                             # Vue 3 前端
│   ├── package.json
│   ├── vite.config.ts                    # Vite + 代理配置
│   ├── index.html
│   └── src/
│       ├── main.ts                       # 入口
│       ├── App.vue                       # 根组件
│       ├── router/                       # 路由
│       ├── stores/                       # Pinia 状态管理
│       │   ├── project.ts                # 项目状态
│       │   └── canvas.ts                 # 画布节点/连线状态
│       ├── api/                          # API 调用层
│       │   ├── project.ts
│       │   ├── node.ts
│       │   └── ai.ts
│       ├── types/                        # TypeScript 类型
│       ├── views/                        # 页面
│       │   ├── ProjectList.vue           # 项目列表
│       │   └── CanvasEditor.vue          # 画布编辑器
│       └── components/
│           ├── canvas/
│           │   └── FreeCanvas.vue        # Vue Flow 画布
│           ├── nodes/                    # 自定义节点
│           │   ├── CharacterNode.vue     # 角色节点
│           │   ├── SceneNode.vue         # 场景节点
│           │   ├── TextNode.vue          # 台词节点
│           │   └── VideoNode.vue         # 视频节点
│           └── panels/                  # 侧面板
│               ├── NodePalette.vue       # 节点面板
│               ├── PropertyPanel.vue     # 属性面板
│               └── AIPanel.vue           # AI 生成面板
│
└── ANALYSIS_REPORT.md                    # 本报告
```

---

## 七、核心数据流

### 7.1 创建节点流程

```
用户点击/拖拽节点面板
    │
    ▼
前端: canvasStore.addNode(projectId, NodeType.CHARACTER, x, y)
    │ 自动填充默认数据 → POST /api/projects/{id}/nodes
    ▼
后端: NodeController.createNode()
    │ 插入 canvas_node 表
    ▼
后端: WebSocket 广播 NODE_CREATED → 所有在线客户端同步
    │
    ▼
前端: store 更新 nodes 数组 → Vue Flow 响应式渲染新节点
```

### 7.2 AI 生成视频流程

```
用户在 AIPanel 输入提示词 → 点击「生成视频」
    │
    ▼
前端: aiApi.generateVideo({prompt, projectId, nodeId})
    │ POST /api/ai/generate-video
    ▼
后端: AIGenerationController.generateVideo()
    │ 插入 ai_task (status=PENDING)
    │ 返回 taskId 给前端
    ▼
后端: @Async executeVideoGeneration()
    │ 1. 更新 status=PROCESSING → WebSocket 推送
    │ 2. HTTP 调用 Seedance 2.0 API
    │ 3. 轮询/等待结果
    │ 4. 更新 status=COMPLETED + result_url → WebSocket 推送
    ▼
前端: WebSocket 收到 AI_TASK_UPDATE
    │ 更新对应视频节点的 videoUrl + status
    │ 视频节点显示播放器
    ▼
用户在画布上看到生成完成的视频
```

---

## 八、扩展性分析

### 8.1 短期可扩展（1-2 周）

| 方向 | 描述 | 复杂度 |
|------|------|--------|
| 图片素材节点 | 支持上传/粘贴图片作为场景/角色参考图 | 低 |
| 提示词模板 | 预设风格模板（古装/现代/科幻...）自动拼接提示词 | 低 |
| 撤销/重做 | 基于命令模式的画布操作历史栈 | 中 |
| 导出脚本 | 将画布内容导出为分镜脚本 PDF/Word | 中 |

### 8.2 中期可扩展（1-3 月）

| 方向 | 描述 | 复杂度 |
|------|------|--------|
| Redis 缓存 | 缓存项目节点数据 + AI 生成结果，减少 MySQL 查询 | 中 |
| RabbitMQ | 将 AI 任务从 @Async 线程池迁移到 MQ，支持优先级和重试 | 中 |
| 多模型接入 | 支持切换其他模型（Sora、Runway、可灵等） | 中 |
| 视频剪辑节点 | 在画布上直接拼接多段 AI 生成的视频 | 高 |

### 8.3 长期可扩展（3-6 月）

| 方向 | 描述 | 复杂度 |
|------|------|--------|
| 多人实时协作（CRDT） | 解决并发编辑冲突，替代当前的简单 WebSocket 广播 | 高 |
| AI 自动补全 | 画几个节点后，AI 自动建议下一步的剧情/角色/场景 | 高 |
| 时间轴视图 | 在画布基础上增加时间轴维度，组织视频顺序 | 高 |
| 一键成片 | 从画布直接渲染完整视频（串联所有视频节点+转场+字幕） | 高 |

---

## 九、技术风险与建议

### 9.1 风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| Vue Flow API 变化 | 前端画布功能受影响 | 固定主版本号，不自动升级 |
| Seedance API 限流 | 视频生成排队积压 | 实现任务队列 + 用户额度管理 |
| 视频生成耗时长 | 用户体验差 | WebSocket 实时推送进度；异步非阻塞 |
| MySQL JSON 查询性能 | 大量节点时查询变慢 | 对常用字段（name、label）冗余存储到独立列 |

### 9.2 建议

1. **先 Mock 后真实**：开发阶段先用 Mock 数据跑通整个流程，AI API Key 配置好后再切换真实调用
2. **渐进增强**：先保证节点 CRUD + 连线 + 画布渲染稳定，再逐步完善 AI 生成和实时协作
3. **监控埋点**：对 AI API 调用成功率、延迟、费用做监控，避免意外账单
4. **提示词工程**：Seedream/Seedance 的提示词质量决定生成效果，建议建立提示词库和最佳实践文档

---

## 十、总结

本项目采用**前后端分离 + 节点画布 + AI 集成**的架构：

- **前端** Vue 3 + Vue Flow 提供直观的节点式剧情编辑器，用户无需编程
- **后端** Spring Boot 提供稳定的 CRUD + WebSocket + 异步 AI 调用服务
- **AI** 豆包 Seedream/Seedance 提供图片和视频生成能力
- **存储** MySQL JSON 字段灵活存储各类型节点数据，兼顾结构化查询和灵活性
- **实时** WebSocket 实现协作编辑和 AI 进度推送

技术选型遵循**最小必要原则**，仅引入当前确实需要的组件（WebSocket、@Async），避免过度设计。架构具备良好的扩展性，可以逐步引入 Redis、MQ、CRDT 等高级特性。
