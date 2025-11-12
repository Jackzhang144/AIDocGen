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

## 交互流程
1. 用户首先注册/登录，拿到 JWT 后才能看到任务、历史和管理员标签页。
2. 粘贴代码并选择语言、格式、宽度等参数提交任务。
3. 前端调用 `POST /docs/write/v3`，获取 `id` 并每 2 秒轮询 `GET /docs/worker/{id}`。
4. 当状态变为 `SUCCEEDED` 时展示 `documentation`/`preview` 字段，并允许 `POST /docs/feedback` 点赞或点踩。
5. “历史记录”标签使用 `/docs/history` 查看分页数据；管理员标签页则调用 `/admin/users`、`/admin/api-keys` 维护权限与 Key。

## 与后端的约定
- 所有请求都走 `/api` 前缀，保持与 Spring Boot `server.servlet.context-path=/api` 一致。
- 后端新增的全局 CORS 白名单位于 `web.cors.allowed-origins`，默认包含 `http://localhost:5173`；线上可通过环境变量覆盖。
- 认证信息保存在本地 `localStorage`，刷新页面后仍会自动附带 `Authorization` 头。

## 可扩展建议
1. 引入 Pinia/Redux 等状态管理，支持多任务同时展示。
2. 将语言/格式列表改为后端动态获取（可新增公开的 `/docs/meta/languages` 接口避免 API-Key）。
3. 接入组件库（Naive UI、Element Plus）以加速 UI 迭代。
4. 增加 E2E 测试（Playwright）覆盖“提交→完成”主路径。
