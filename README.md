# AIDocGen 文档生成平台

Spring Boot + Vue 的全栈应用，提供异步代码文档生成、历史留档、管理员配额与 API-Key 管理，并兼容 Mintlify Writer 风格的公共 API。

## 核心特性
- 📄 **异步文档生成**：`/docs/write/v3` 入队，`/docs/worker/{id}` 轮询，可回填 `feedback/intro`。
- 📚 **历史检索**：按关键字、语言、来源分页查看生成记录。
- 🔐 **双重鉴权**：内部接口用 JWT，公共 `/api/v1/**` 用 `API-KEY`，并支持滑动窗口限流（Redis→内存降级）。
- 🧑‍💼 **管理员面板**：网页端即可调整用户角色/配额、创建或删除 API-Key。
- 🤖 **多模型网关**：OpenAI、DeepSeek 或本地启发式兜底，可在配置中热切换。

## 目录结构
```
.
├── src/                         # Spring Boot 后端（context-path=/api）
│   └── main/java/com/codecraft/aidoc
│       ├── controller/          # Doc/Auth/Admin/Public 控制器
│       ├── service/             # DocJob/Documentation/RateLimiter 等服务
│       ├── gateway/             # OpenAI / DeepSeek / Noop 网关
│       └── security/            # JWT + API-Key 过滤器
├── frontend/                    # Vue 3 + Vite 管理控制台
├── docs/                        # API、架构、前端、使用说明
└── ProjectDocumentationGenerator/ # 兼容官方插件的历史代码
```

## 环境要求
| 组件 | 版本 |
| --- | --- |
| JDK | 17 |
| Maven | 3.9+ |
| Node.js | 18+（运行前端） |
| MySQL | 8.x（默认库名 `aiddoc`） |
| Redis | 6+（可选，提供分布式限流） |

执行 `src/main/resources/schema.sql` 以初始化 `docs`、`users`、`doc_jobs`、`api_keys` 等表：
```bash
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS aiddoc CHARACTER SET utf8mb4;"
mysql -uroot -p aiddoc < src/main/resources/schema.sql
```

## 配置说明
- **数据库**：通过 `APP_DATASOURCE_URL/USERNAME/PASSWORD` 或直接编辑 `application.yml`。
- **Redis（可选）**：`spring.data.redis.*`，缺省则自动退回本地桶算法。
- **AI 网关**：
  ```bash
  AI_GATEWAY_ENABLED=true
  AI_GATEWAY_PROVIDER=openai   # 或 deepseek
  OPENAI_API_KEY=sk-xxx
  DEEPSEEK_API_KEY=sk-xxx
  ```
- **管理员账号**：`security.admin.username/password/email`，由 `AdminAccountInitializer` 在启动时确保存在（默认配额 `-1`，即无限制）。
- **前端代理**：`frontend/.env.local` 中调整 `VITE_API_BASE_URL`（默认 `http://localhost:8080/api`）。

## 快速开始
1. **后端**
   ```bash
   mvn clean package
   mvn spring-boot:run
   # 服务默认监听 http://localhost:8080/api
   ```

2. **前端**
   ```bash
   cd frontend
   npm install
   npm run dev        # http://localhost:5173，已配置 /api 代理
   ```

3. **登录体验**
   - 首位注册用户会自动成为管理员，可通过 UI 创建额外用户、调整配额、生成 API-Key。
   - 提交文档任务后，前端每 2 秒轮询 `/docs/worker/{id}`，完成即可查看 Output 并提交反馈。

4. **（可选）官方插件联调**
   - 进入 `ProjectDocumentationGenerator/` 按原项目说明编译。
   - 将插件的服务地址指向 `http://localhost:8080/api`。
   - 通过网页管理员面板创建 API-Key 后，在插件端填写原始 Key 以调用 `/api/v1/document`。

## 文档与接口
- `docs/api.md`：JWT/API-Key 鉴权、异步任务、公共 API 及错误码一览。
- `docs/architecture.md`：后端模块、模型网关与部署拓扑。
- `docs/frontend.md`：Vue 组件、API 客户端、交互流程。
- `docs/usage.md`：端到端启动、联调与常见问题。

常用健康检查：
```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/actuator/health
```

## 贡献与测试
- 后端测试：`mvn test`
- 前端检查：`npm run build`
- 建议在提交前同步更新 `docs/*.md`（或本 README）以反映接口与配置变化。欢迎通过 Issue/PR 分享新的模型网关或前端改进思路。
