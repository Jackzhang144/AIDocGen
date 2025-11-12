# Vue Web 前端

本目录介绍 `frontend/`（Vue 3 + Vite）的结构、环境变量和与 Spring Boot 后端的联调方式。

## 目录结构
```
frontend/
├── index.html
├── package.json
├── src
│   ├── App.vue                   # 顶层布局，含登录/标签页切换
│   ├── main.js
│   ├── api/
│   │   └── client.js             # Axios 实例 + JWT 拦截器
│   ├── components/
│   │   ├── AuthPanel.vue         # 登录/注册
│   │   ├── DocGenerator.vue      # 提交/轮询/反馈
│   │   ├── HistoryPanel.vue      # 历史记录
│   │   └── AdminPanel.vue        # 用户+API-Key 管理
│   ├── stores/
│   │   └── auth.js               # 极简全局状态（token + profile）
│   └── style.css
└── vite.config.js
```

## 运行
```bash
cd frontend
npm install
npm run dev         # 启动 Vite 开发服务器，默认端口 5173
```
- `vite.config.js` 会将 `/api` 代理到 `http://localhost:8080`，方便本地联调。
- 构建生产包：`npm run build`，产物位于 `frontend/dist/`。

## 环境变量
在 `frontend/.env` 或 `.env.local` 中设置：

| 变量 | 说明 | 默认值 |
| --- | --- | --- |
| `VITE_API_BASE_URL` | 前端直连的 API 地址 | `http://localhost:8080/api` |
| `VITE_API_PROXY_TARGET` | Dev Server 代理目的地（仅 `npm run dev` 使用） | `http://localhost:8080` |
| `VITE_HTTP_TIMEOUT_MS` | Axios 请求超时 | `20000` |

## 主要组件
- `AuthPanel.vue`：登录/注册表单，共享一个 `mode` 切换；成功后调用 `useAuthStore.setAuth` 并将 `token`、`user` 分别写入 `localStorage`（键 `aidoc_token` / `aidoc_user`）。
- `DocGenerator.vue`：核心任务面板，提供 `code/context` 双输入、语言与文档格式下拉、注释包裹/选区模式复选框；提交后记录 `jobMeta` 并每 2 秒轮询 `/docs/worker/{id}`，生成成功后允许基于 `feedbackId` 点赞/点踩。
- `HistoryPanel.vue`：封装分页、刷新与 `keyword/language/source` 三种筛选，调用 `/docs/history` 并以内联 `<details>` 展示 `outputPreview`。
- `AdminPanel.vue`：分区展示用户列表与 API-Key 表，支持就地修改 email/role/apiQuota、创建/删除 Key，并将最新创建的原始 Key 以提示方式回显。
- `stores/auth.js`：极简状态容器，暴露 `isAuthenticated/isAdmin/isPremium` 计算属性供 `App.vue` 切换 Tab。

## API 客户端
`src/api/client.js` 基于 Axios 创建统一实例并附带 JWT Header，暴露以下方法：

| 方法 | 描述 |
| --- | --- |
| `submitDocJob(payload)` / `fetchJobStatus(id)` / `submitFeedback()` | 对应 `/docs/write/v3`、`/docs/worker/{id}`、`/docs/feedback`。 |
| `login()` / `register()` | `/auth/*`。 |
| `fetchHistory(params)` | `/docs/history`，用于历史面板分页与过滤。 |
| `fetchUsers()`、`updateUser()` | `/admin/users` 列表 + 编辑。 |
| `listApiKeys()`、`createApiKey()`、`deleteApiKey()` | `/admin/api-keys` CRUD。 |

## 交互流程
1. 用户首先注册/登录，拿到 JWT 后才能看到任务、历史和管理员标签页。
2. 粘贴代码并选择语言、格式、宽度等参数提交任务。
3. 前端调用 `POST /docs/write/v3`，获取 `id` 并每 2 秒轮询 `GET /docs/worker/{id}`。
4. 状态为 `SUCCEEDED` 时展示 `documentation`/`preview`，并允许 `POST /docs/feedback` 点赞或点踩。
5. “历史记录”标签使用 `/docs/history` 查看分页数据（可刷新/筛选）；管理员标签页则调用 `/admin/users`、`/admin/api-keys` 维护权限与 Key。

## 与后端的约定
- 所有请求都走 `/api` 前缀，保持与 Spring Boot `server.servlet.context-path=/api` 一致。
- 后端新增的全局 CORS 白名单位于 `web.cors.allowed-origins`，默认包含 `http://localhost:5173`；线上可通过环境变量覆盖。
- 认证信息保存在本地 `localStorage`，刷新页面后仍会自动附带 `Authorization` 头。

## 可扩展建议
1. 引入 Pinia/Redux 等状态管理，支持多任务同时展示。
2. 将语言/格式列表改为后端动态获取（可新增公开的 `/docs/meta/languages` 接口避免 API-Key）。
3. 接入组件库（Naive UI、Element Plus）以加速 UI 迭代。
4. 增加 E2E 测试（Playwright）覆盖“提交→完成”主路径。
