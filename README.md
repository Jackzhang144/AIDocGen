# SmartCodeEditor

本仓库提供「本地文件编辑 + AI 注释/解释/文档」的一体化体验，包含 Spring Boot 后端与 Vite + React 前端。前端使用浏览器原生文件系统权限编辑本地文件，后端负责 AI 调用、用户认证与调用统计。

## 功能亮点
- Monaco 编辑器 + 文件夹选择：可打开本地工程，浏览/展开目录并编辑文件。
- AI 能力：选中文本后可生成注释、解释或文档摘要，结果在右侧面板呈现；语言支持中/英文切换。
- 身份与配额：支持注册/登录，角色区分 USER/MEMBER/ADMIN；USER 受 `ai.daily-limit` 限制（默认 100 次/天）。
- 管理后台：ADMIN 可在前端管理用户（增删改查、修改角色、重置密码）并查看调用总量。
- 日志与配置：AI 请求写入 `ai_request_logs`，启动时按配置自动创建管理员账号。

## 快速开始
前置依赖：Node 18+、Java 17、Maven 3.9+，以及可用的 MySQL（或兼容数据库）。

1) 配置后端  
复制 `backend/src/main/resources/application-example.yml` 为 `application.yml`，填写数据库与 DeepSeek 密钥，或使用环境变量覆盖。

2) 启动后端
```bash
cd backend
mvn clean package
mvn spring-boot:run
```
默认端口 `8080`。可用 `mvn test` 在 H2 上运行集成测试。

3) 启动前端
```bash
cd frontend
npm install
npm run dev
```
默认端口 `5173`，已开放本地 CORS 访问后端。构建发布使用 `npm run build`，代码检查使用 `npm run lint`。

## 目录结构
- `backend/src/main/java/com/codecraft`：`controller`（AI/Auth/Admin）、`service`（DeepSeek 调用与日志）、`entity`、`repository`、`security`。
- `backend/src/main/resources`：`application.yml`/`application-example.yml`、`schema.sql` 数据表定义。
- `frontend/src`：React 组件（文件侧边栏、Monaco 编辑器、AI 面板、认证/管理页）、`i18n.js` 语言包、`config.js` 后端地址。

## 配置说明
后端主要配置（YAML 或环境变量均可）：
- `spring.datasource.*`：数据库连接；测试环境使用 H2。
- `deepseek.api.key` / `DEEPSEEK_API_KEY`：AI Key（必填），`deepseek.api.url`、`deepseek.api.timeout-ms` 可调整。
- `ai.daily-limit`：USER 角色每日调用上限，默认 100。
- `admin.username` / `admin.password`：启动时自动创建的管理员账号。
- `jwt.secret` / `jwt.expiration-ms`：JWT 签名密钥与过期时间。

前端配置：
- `VITE_API_URL`：前端调用的后端根地址，默认 `http://localhost:8080`。

## API 概览
- `POST /api/auth/register`：注册，返回 `username/role/token`。
- `POST /api/auth/login`：登录，返回 JWT。
- `POST /api/ai/process`：AI 处理，body 包含 `type`（comment/explain/document）、`code`、`fileName`、`context`（文档模式使用）、`language`（zh/en）；需携带 `Authorization: Bearer <token>`。
- `GET /api/admin/users`：列出用户及调用总量；ADMIN 角色。
- `POST /api/admin/users` / `PUT /api/admin/users/{id}` / `DELETE /api/admin/users/{id}`：创建、更新、删除用户（不可修改/删除自身管理员）。

## 数据表
- `users`：字段 `username/password/role/created_at/updated_at`。
- `ai_request_logs`：记录请求类型、文件名、用户名、前 500 字符的代码片段与创建时间。

## 开发小贴士
- 生成注释时会直接替换编辑器选区；保存按钮会通过 File System Access API 写回本地文件。
- 若调整端口或跨域策略，请同时更新 `frontend/src/config.js` 与后端 CORS 配置。
- 请勿提交真实密钥或数据库口令，可在部署环境通过环境变量注入。
