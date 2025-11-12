# Project Documentation Generator（前后端分离）

参考 Mintlify Writer 的文档生成思路，我们实现了 Spring Boot 3.2 后端 + Vue 3 前端的最小可用产品。仓库砍掉登录、会员、团队等复杂功能，聚焦“代码 → 文档”的主链路，便于课程大作业或小团队协作快速交付。

## ✨ 目前具备的能力
- **异步文档生成**：`/docs/write/v3` 与 `/docs/write/v3/no-selection` 和原协议保持一致，线程池调度后台任务，并允许 `/docs/worker/{id}` 轮询。
- **任务持久化**：所有文档任务都会写入 `doc_jobs` 表，服务重启后会自动恢复 PENDING/IN_PROGRESS 任务并继续执行，保证“提交必有结果”。
- **模型网关与兜底**：`DocumentationService` 优先调用 OpenAI/DeepSeek，失败后自动回退启发式模板，保障“有请求就有响应”。
- **角色与配额**：新增注册/登录模块，用户分为普通（限流）、高级（无限制）与管理员（可配置角色、配额和 API-Key）。
- **历史检索面板**：登录后可在 Web 端查看个人生成历史，管理员可按用户过滤。
- **公共 API**：`/api/v1/**` 使用 API-Key + 限流暴露同步文档和语言/格式列表，兼容历史客户端。
- **管理员控制台**：Vue 前端新增标签页，用于调整用户角色/配额并创建、删除 API-Key。

## 🧰 技术栈
| 类别 | 组件 |
| --- | --- |
| 后端 | Spring Boot 3.2 + Maven |
| 持久化 | MyBatis-Plus + MySQL (`docs`、`api_keys`) |
| 限流 | Redis（可选，自动降级到本地桶算法） |
| 模型 | OpenAI / DeepSeek / 启发式兜底 |
| 安全 | Spring Security + API-Key 头校验 |
| 前端 | Vue 3 + Vite + Axios |

## 📐 非功能性需求
- **可靠性**：后台任务落盘并在启动时恢复，线程池 + 模型兜底避免“黑洞请求”；健康探活接口 `/`、`/health` 确保容器可观测。
- **性能与扩展性**：Redis 滑动窗口限流将 `/api/v1/**` 峰值限制在 15 分钟 100 次，自定义 `apiQuota` 约束普通用户提交频率；前端轮询周期 2 秒，避免无效请求。
- **安全性**：同步 API 仍采用 API-Key 鉴权，Web 控制台新增 JWT 认证 + 角色控制 + 管理员面板，CORS 白名单可配置。

## 📁 目录
```
src/main/java/com/codecraft/aidoc    # Spring Boot 源码
src/main/resources/schema.sql        # docs / api_keys 表结构
docs/                                # 架构、接口、使用说明
frontend/                            # Vue 3 + Vite 前端
ProjectDocumentationGenerator/       # 原 Mintlify Writer 仓库（只读参考）
```

## ⚙️ 后端启动
1. 安装 JDK 17、Maven 3.9+、MySQL 8、Redis 6（可选）。
2. 初始化数据库：
   ```bash
   mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS aiddoc CHARACTER SET utf8mb4;"
   mysql -uroot -p aiddoc < src/main/resources/schema.sql
   ```
3. （可选）配置模型网关：
   ```bash
   export AI_GATEWAY_ENABLED=true
   export AI_GATEWAY_PROVIDER=openai   # 或 deepseek
   export OPENAI_API_KEY=sk-xxxx       # provider=openai 时必填
   ```
4. 启动服务：
   ```bash
   mvn clean package
   mvn spring-boot:run
   ```
   默认监听 `http://localhost:8080/api`，CORS 白名单为 `http://localhost:5173`。

## 🖥️ Web 前端
前端位于 `frontend/`：
```bash
cd frontend
npm install
npm run dev            # http://localhost:5173
```
- 可在 `.env.local` 中设置 `VITE_API_BASE_URL` 指向部署好的后端（默认为 `http://localhost:8080/api`）。
- 若 dev server 与后端端口不一致，`vite.config.js` 内置代理 `/api -> http://localhost:8080`，也可通过 `VITE_API_PROXY_TARGET` 调整。
- 页面展示：登录/注册 → 代码提交/轮询 → 历史记录与反馈 → 管理员标签页配置用户与 API-Key。

### 账号与角色
- 管理员账号在 `application.yml -> security.admin.*` 中配置，启动时自动确保存在（默认无限额度）。
- 普通用户默认 15 分钟 100 次异步任务配额，可由管理员在前端面板调整。将角色改为 `PREMIUM` 可获得无限额度。
- 管理员面板还可以：
  - 查看/编辑所有用户的邮箱、角色、配额；
  - 创建/删除 `/api/v1/**` 所需的 API-Key，并即时看到 SHA-1 哈希值。

## 🔌 兼容官方客户端
`ProjectDocumentationGenerator/` 保留 Mintlify Writer 原版代码，可按需参考。若需要调用 `/api/v1/**`：
1. 在 `api_keys` 表插入 `hashed_key = SHA1(raw-key)`。
2. 请求头携带 `API-KEY: <raw-key>`。

## ✅ 测试
```bash
mvn test
```

## 📚 附加文档
- `docs/architecture.md`：整体架构与依赖关系
- `docs/api.md`：接口定义与错误码
- `docs/usage.md`：端到端启动与排查
- `docs/frontend.md`：Vue 前端结构、环境变量与部署

> 当前阶段的目标是打磨“代码输入 → 文档输出”的主链路。后续若需重新引入账号、团队或桌面 UI，可在此基础上逐步迭代。
