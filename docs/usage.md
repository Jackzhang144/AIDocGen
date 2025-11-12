# 使用说明

本文档提供端到端的启动流程，涵盖环境准备、配置、运行与常见问题排查。

## 1. 环境准备
| 组件 | 要求 |
| --- | --- |
| JDK | 17 |
| Maven | 3.9+ |
| 数据库 | MySQL 8（默认库名 `aiddoc`） |
| 缓存/限流 | Redis 6+（可选） |

### 初始化数据库
```bash
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS aiddoc CHARACTER SET utf8mb4;"
mysql -uroot -p aiddoc < src/main/resources/schema.sql
```
> 新版 `schema.sql` 会额外创建 `users`、`doc_jobs` 表，请确保执行后再启动。

### Redis
本地启动 `redis-server` 或在 `.env` 中填入远程地址：
```
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_DB=0
```
> 未搭建 Redis 时，`RateLimiterService` 会自动降级为本地桶算法，仍可在开发环境运行，但无法享受跨实例限流。

### （可选）启用 AI 模型
```
AI_GATEWAY_ENABLED=true
AI_GATEWAY_PROVIDER=openai   # 或 deepseek
OPENAI_API_KEY=sk-xxxxx      # provider=openai 时配置
DEEPSEEK_API_KEY=sk-xxxxx    # provider=deepseek 时配置
```
未配置时自动使用启发式生成。

## 2. 启动后端
```bash
mvn clean package
mvn spring-boot:run
```

默认监听 `http://localhost:8080/api`。常用环境变量：
```
APP_DATASOURCE_URL=jdbc:mysql://localhost:3306/aiddoc?serverTimezone=UTC
APP_DATASOURCE_USERNAME=root
APP_DATASOURCE_PASSWORD=secret
```

验证服务：
```bash
curl http://localhost:8080/api/actuator/health
```

## 3. 启动 Web 前端
```bash
cd frontend
npm install
npm run dev               # http://localhost:5173
```
- `.env.local` 中的 `VITE_API_BASE_URL` 默认为 `http://localhost:8080/api`；若部署在不同主机，请同步修改。
- 登录后可在“文档生成 / 历史记录 / 管理员”三个标签间切换。
- 管理员账号由 `security.admin.username/password` 配置，服务启动后会自动确保该账号存在（默认配额 -1）。

## 4. （可选）兼容官方插件
仓库仍保留 `ProjectDocumentationGenerator/`，若需要使用 VS Code / JetBrains 插件：
1. 按官方说明启动插件。
2. 将 `MINTBASE`（或等价配置）指向 `http://localhost:8080/api`。
3. 若调用 `/api/v1/document`，可通过网页端管理员面板或 `POST /admin/api-keys` 创建 Key（后端会自动保存 SHA-1 哈希并回显原始值）；也可手工向 `api_keys` 表插入 `hashed_key`。

## 5. 功能验证
1. 注册并登录（若为首次部署，注册者会自动成为管理员）。
2. 在“文档生成”标签提交代码，确认返回的 `id` 展示在任务卡片上。
3. 观察轮询（或 `curl /docs/worker/{id}`），状态应从 `PENDING` → `IN_PROGRESS` → `SUCCEEDED`，刷新页面后依然能查询该任务（验证持久化）。
4. 打开“历史记录”标签，查看刚才的任务记录及反馈。
5. 在“管理员”标签修改某个用户的配额/角色，并创建新的 API-Key，数据库 `api_keys` 表应新增一条带有 SHA-1 哈希的记录。
6. 若启用公共 API，使用 `curl -H "API-KEY: <raw>"` 调用 `/api/v1/document`，确认限流与鉴权生效。

## 6. 常见问题
| 问题 | 处理方式 |
| --- | --- |
| `RATE_LIMITED` | 检查 Redis 是否可用，或稍后再试；默认 15 分钟 100 次。 |
| `AUTHENTICATION_FAILED` | 确认 `/api/v1/**` 请求携带 `API-KEY`，或 key 是否存在于 `api_keys` 表。 |
| OpenAI 调用失败 | 检查 `AI_GATEWAY_ENABLED`、`OPENAI_API_KEY`、网络连通性；日志会输出中文错误提示。 |
| API-Key 接口 401 | 确认请求头 `API-KEY` 与数据库 `api_keys.hashed_key` 对应（SHA-1）。 |
| 前端无法访问后端 | 核对 `web.cors.allowed-origins`、`VITE_API_BASE_URL` 与代理地址是否一致。 |

## 7. 日志 & 调试
- 后端：默认 `INFO` 级别，可在 `logback-spring.xml` 调整。
- 前端：Vite 控制台会输出请求错误信息，可在浏览器 DevTools Network 面板查看详细请求。

## 8. 推荐开发流程
1. `mvn test` 确认后端单元测试通过。
2. 启动后端与 `frontend/` Vite 服务，完成主流程回归。
3. 若需要兼容官方插件，再进入 `ProjectDocumentationGenerator/` 做额外联调。
4. 提交前更新文档（`docs/*.md`）并在提交信息中注明测试结果。

完成以上步骤后，即可得到一个可直接演示的前后端分离 AI 文档生成工具；如需兼容官方插件，可在此基础上扩展。
