# 架构概览

本文档介绍 AIDocGen 的整体架构，便于快速理解 Spring Boot 后端与 Vue Web 前端之间的协同关系。

## 后端

- **技术栈**：Spring Boot 3 + MyBatis-Plus + Redis + MySQL，异步任务使用 `ThreadPoolTaskExecutor`。
- **核心模块**：
  - `DocController` + `DocJobService`：接收与调度异步文档生成任务，任务落盘于 `doc_jobs` 表，可在重启后恢复；并暴露查询、反馈接口。
  - `DocumentationService`：封装模型调用（OpenAI/DeepSeek 网关）与启发式兜底逻辑，始终返回结构化的 `DocGenerationResult`。
  - `DocService`：将生成记录及反馈写入 MySQL，配合 `TelemetryService` 做埋点。
  - `DocHistoryController` + `DocHistoryService`：基于 MyBatis-Plus 分页查询历史记录，支持关键词/语言/来源筛选，并对非管理员自动加上用户隔离。
  - `RateLimiterService`：基于 Redis 脚本的滑动窗口限流，自动降级到本地桶算法。
  - `SecurityConfig`：以链式 Filter 组合 `ApiKeyAuthenticationFilter` 与 `JwtAuthenticationFilter`，实现在 `/api/v1/**` 使用 API-Key、在 `/docs/**` `/admin/**` 使用 JWT。
  - `AuthController`：管理注册/登录，签发 JWT。
  - `AdminController` + `ApiKeyService`：提供用户角色/配额与 API-Key 管理接口，对应前端管理员面板。
  - `AdminAccountInitializer`：根据 `security.admin.*` 配置在启动时自动确保管理员账号存在。
- **AI 模型网关**：
  - `ModelGateway` 定义统一接口；
  - `OpenAiModelGateway` 与 `DeepSeekModelGateway` 提供不同供应商的实现，均通过 `ModelGatewayConfig` 根据 `ai.gateway.provider` 自动切换；
  - `NoopModelGateway` 在网关关闭或配置缺失时返回空结果，触发本地启发式兜底；
  - 统一配置封装在 `ModelGatewayProperties`，可通过 `OPENAI_API_KEY` / `DEEPSEEK_API_KEY` 等变量注入。
- **数据流**：
  1. 前端或任何 HTTP 客户端调用 `/docs/write/v3` 创建任务；
  2. `DocJobService` 将请求提交至线程池执行，期间调用 `DocumentationService`；
  3. `DocumentationService` 首先构造 Prompt 调用大模型，失败时回到启发式生成；
  4. 结果写入 `docs` 表并通过 `TelemetryService` 上报；
  5. Job 会在 `doc_jobs` 表更新状态/结果，客户端轮询 `/docs/worker/{id}` 获取状态，或通过 `/docs/feedback` 提交反馈；
  6. `DocHistoryService` 同步写入裁剪后的 `output/prompt`，供 `/docs/history` 做分页展示；
  7. 服务启动时会扫描 `doc_jobs` 中 PENDING/IN_PROGRESS 的任务并重新派发。

## Web 前端

- `frontend/` 基于 Vue 3 + Vite，提供登录/注册、任务提交、历史列表与管理员面板；
- Axios 客户端使用 `VITE_API_BASE_URL`（默认为 `http://localhost:8080/api`）联通后端，并带有统一的错误拦截；
- 轮询逻辑位于 `DocGenerator.vue`，每 2 秒调用 `/docs/worker/{id}`，并在成功后允许 `/docs/feedback` 提交结果；
- `HistoryPanel.vue` 提供关键词/语言/来源筛选与分页；`AdminPanel.vue` 同屏维护用户角色、配额以及 API-Key；
- 登录态保存在本地 `localStorage`（JWT），`src/api/client.js` 会自动附带 `Authorization` 头；管理员标签页调用 `/admin/**` 接口。

## 部署拓扑

```
Vue Web ──HTTP──> Spring Boot 后端 ──> OpenAI/DeepSeek(可选)
                        │
                        ├── MySQL：存储 docs / api_keys
                        └── Redis：限流（可选）
```

所有日志默认输出到控制台，可通过 `logback-spring.xml` 配置文件重定向。

## 外部依赖

- **MySQL**：存储 `docs`、`doc_jobs`、`users`、`api_keys` 等核心数据；见 `src/main/resources/schema.sql`。
- **Redis**（可选）：用于分布式限流与未来的消息排队；未配置时自动退化为本地桶算法。
- **OpenAI / DeepSeek**（可选）：配置 `AI_GATEWAY_*` 环境变量后启用真实模型；未配置时自动回退到启发式生成。
- **环境变量**：`security.admin.*` 注入默认管理员；`APP_DATASOURCE_*` 指定数据库连接；`web.cors.*` 控制跨域。
- **配置模板**：仓库提供 `src/main/resources/application-example.yml`；复制为 `application.yml` 后填写私密信息即可，真实文件不会进入 Git。

## 运行方式

1. **启动后端**
   ```bash
   mvn spring-boot:run
   ```
2. **启动 Web 前端**：进入 `frontend/`，执行 `npm install && npm run dev`；或 `npm run build` 后托管于任意静态服务器。

更多细节请参阅 `docs/api.md`、`docs/frontend.md` 与 `docs/usage.md`。
