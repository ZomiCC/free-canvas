# AGENTS.md — 自由画布 (Free Canvas)

## 项目概述

节点式 AI 剧情编辑器。用户在画布上操作角色/场景/台词/视频四种节点，连线建立逻辑关系，通过豆包大模型将创意转化为影像。

- **前端**: Vue 3 + Vue Flow + Element Plus + Pinia
- **后端**: Spring Boot 3 + MyBatis-Plus + H2/MySQL + WebSocket
- **AI**: 豆包 Seedream 3.0 (生图) + Seedance 2.0 (生视频)
- **存储**: 火山 TOS (S3 兼容)

## 目录结构

```
free-canvas/
├── AGENTS.md                          ← 本文件
├── ANALYSIS_REPORT.md                 ← 架构分析报告
├── .env.example                       ← 环境变量模板（无真实值）
│
├── backend/                           ← Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/freecanvas/
│       ├── FreeCanvasApplication.java  ← 启动类
│       ├── controller/                ← REST 控制器
│       │   ├── ProjectController.java
│       │   ├── NodeController.java     ← 节点 + 连线 CRUD
│       │   └── AIGenerationController.java ← AI 生成 + 智能提示词
│       ├── service/                   ← 业务接口 + 实现
│       │   ├── ProjectService.java
│       │   ├── NodeService.java        ← 含 getConnectedNodes()
│       │   ├── AIGenerationService.java
│       │   ├── CloudStorageService.java ← TOS 上传接口
│       │   ├── AsyncAiExecutor.java    ← @Async 独立 Service（解决代理问题）
│       │   ├── PromptBuilder.java      ← 智能提示词拼接
│       │   └── impl/
│       │       ├── ProjectServiceImpl.java
│       │       ├── NodeServiceImpl.java
│       │       ├── AIGenerationServiceImpl.java
│       │       └── TosStorageServiceImpl.java ← AWS S3 SDK 操作 TOS
│       ├── model/
│       │   ├── entity/                 ← JPA 实体 (4 张表)
│       │   ├── dto/                    ← 请求/响应 DTO
│       │   └── enums/                  ← NodeType / VideoStatus
│       ├── repository/                 ← MyBatis-Plus Mapper
│       ├── config/                     ← WebSocket / Async / 异常处理
│       └── websocket/
│           └── CanvasWebSocketHandler.java ← 按 projectId 广播
│
├── frontend/                          ← Vue 3 前端
│   ├── vite.config.ts                 ← 含 @ 别名 + API 代理
│   └── src/
│       ├── main.ts                    ← 入口 (Pinia + ElementPlus + Router)
│       ├── App.vue
│       ├── router/index.ts            ← / 项目列表, /canvas/:id 编辑器
│       ├── stores/
│       │   ├── canvas.ts              ← 节点/连线状态 + Vue Flow 转换
│       │   └── project.ts             ← 项目列表状态
│       ├── api/                       ← Axios 封装
│       │   ├── project.ts
│       │   ├── node.ts
│       │   └── ai.ts
│       ├── types/index.ts             ← 全部 TypeScript 类型
│       ├── views/
│       │   ├── ProjectList.vue        ← 项目列表页
│       │   └── CanvasEditor.vue       ← 画布编辑器主页
│       └── components/
│           ├── canvas/FreeCanvas.vue   ← Vue Flow 画布
│           ├── nodes/                 ← 自定义节点组件
│           │   ├── CharacterNode.vue
│           │   ├── SceneNode.vue
│           │   ├── TextNode.vue
│           │   └── VideoNode.vue
│           └── panels/               ← 侧面板
│               ├── NodePalette.vue     ← 新增节点面板
│               ├── PropertyPanel.vue   ← 属性编辑面板
│               └── AIPanel.vue         ← AI 生成面板
```

## 开发命令

```bash
# 前端
cd frontend
npm install                    # 安装依赖
npm run dev                    # 启动开发服务器 (localhost:3000)
npm run build                  # 生产构建

# 后端
cd backend
mvn clean compile              # 编译
mvn spring-boot:run            # 启动 (开发模式, H2 内存数据库)
```

## 环境变量

所有密钥通过环境变量注入，**绝不硬编码**：

| 变量 | 说明 | 必填 |
|------|------|------|
| `DOUBAO_API_KEY` | 豆包大模型 API Key | 是 |
| `TOS_ACCESS_KEY_ID` | 火山 TOS Access Key | 否 (开发不需) |
| `TOS_SECRET_ACCESS_KEY` | 火山 TOS Secret Key | 否 (开发不需) |
| `MYSQL_USERNAME` | MySQL 用户名 | 否 (默认 root) |
| `MYSQL_PASSWORD` | MySQL 密码 | 否 (默认 root) |

## 数据库

- **开发模式** (`application-dev.yml`): H2 内存数据库，无需安装 MySQL
- **生产模式** (`application.yml`): MySQL 8.0，自动建表

### 核心表

| 表 | 说明 | 关键字段 |
|----|------|----------|
| `project` | 项目 | name, description |
| `canvas_node` | 画布节点 | project_id, node_type(CHARACTER/SCENE/TEXT/VIDEO), pos_x, pos_y, node_data(JSON) |
| `node_connection` | 节点连线 | source_node_id, target_node_id, source_handle, target_handle |
| `ai_task` | AI 生成任务 | task_type(IMAGE/VIDEO), prompt, status, result_url |

## API 概览

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET/POST/PUT/DELETE` | `/api/projects[/{id}]` | 项目 CRUD |
| `GET/POST/PUT/DELETE` | `/api/projects/{id}/nodes[/{nodeId}]` | 节点 CRUD |
| `GET/POST` | `/api/projects/{id}/connections` | 连线查询/创建 |
| `POST` | `/api/ai/generate-image` | 生图 (Seedream) |
| `POST` | `/api/ai/generate-video` | 生视频 (Seedance) |
| `POST` | `/api/ai/smart-generate-video` | **智能生视频** (自动拼接关联节点提示词) |
| `GET` | `/api/ai/preview-prompt/{projectId}/{nodeId}` | 预览智能提示词 |
| `GET` | `/api/ai/task/{taskId}` | 查询生成任务状态 |
| `WS` | `/ws/canvas/{projectId}` | 实时协作 WebSocket |

## 关键架构决策

### 1. @Async 独立 Service
`AsyncAiExecutor` 是独立的 `@Service` 类，而非 `AIGenerationServiceImpl` 内部方法。**原因**: Spring AOP 代理无法拦截类内部方法调用（self-invocation），必须通过独立 Bean 注入才能让 `@Async` 生效。

### 2. PromptBuilder 智能拼接
生成视频时，`PromptBuilder` 自动读取视频节点通过连线关联的所有角色/场景/台词节点的数据，拼接为复合提示词。例如："场景「桃园」在涿郡城外黄昏 + 角色「关羽」45岁男性忠义勇猛 + 台词「同心协力，救困扶危」"。

### 3. 节点数据用 MySQL JSON 列
不同节点类型结构迥异，MySQL JSON 字段 + MyBatis-Plus 自动序列化，避免 EAV 反模式或多表继承的复杂性。

### 4. WebSocket 按项目广播
`CanvasWebSocketHandler` 维护 `projectId → Set<Session>` 映射，同项目所有在线用户收到同样的节点变更推送，实现简单多人协作。

### 5. H2 + MySQL 双数据库
Spring Profile 切换：`dev` 用 H2 内存库（零安装启动），`prod` 用 MySQL。只需改 `spring.profiles.active`。

## 代码规范

### 后端
- **分层**: Controller → Service → Repository，Controller 不包含业务逻辑
- **DTO/Entity 分离**: 请求用 DTO（含校验注解），存储用 Entity（含逻辑删除）
- **事务**: Service 层 `@Transactional`，AI 异步调用除外
- **异常**: 统一 `GlobalExceptionHandler`，返回 `{"error": "..."}`

### 前端
- **路径别名**: `@/` 映射 `src/`
- **组件通信**: 父传子 Props，子传父 Emits，跨组件用 Pinia Store
- **节点数据解析**: `store/canvas.ts` 的 `parseNodeData()` 统一处理 JSON → 对象
- **Vue Flow 集成**: Store 中的 `flowNodes/flowEdges` 计算属性自动转换内部格式

## 安全

- 🔴 API Key / AK/SK **绝不硬编码**，只用 `${ENV_VAR}`
- 🔴 `.env` 文件已在 `.gitignore`
- 🔴 代码中遗留真实密钥的历史已通过 `git commit --amend` 清除
- 🟡 Token 不在前端 Cookie/Storage 中暴露（可考虑 BFF 模式）

## 已知待优化

1. 生成进度条目前仅 WebSocket 推送状态文本，缺少百分比
2. 撤销/重做功能未实现
3. 视频预签名上传 URL 端已写好但前端未对接
4. 多人协作目前是简单广播，无冲突解决（CRDT 计划中）
