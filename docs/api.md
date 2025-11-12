# 接口说明

后端保持与 Mintlify Writer API 尽量一致，所有路径均位于 `/api`。响应结构统一为：

```json
{
  "code": 0,
  "message": "操作描述",
  "data": {}
}
```

`code != 0` 代表业务异常，客户端需根据 `code` / `message` 判断。

## 文档生成

### `POST /docs/write/v3`
- **用途**：提交「选区」代码生成任务。
- **请求体**（关键字段）：

| 字段 | 说明 |
| --- | --- |
| `code` | 选中代码片段（必填） |
| `languageId` | 语言标识（如 `python`、`java`） |
| `commented` | 是否需包裹注释符 |
| `docStyle` | 文档格式 ID，缺省为自动检测 |
| `userId` / `email` | 用于配额与埋点 |
| `source` | 调用来源（`electron`、`web` 等） |

- **响应**：`{ "id": "<job-id>" }`

### `POST /docs/write/v3/no-selection`
- 与 `/docs/write/v3` 类似，但通过 `context` + `location`/`line` 解析函数体，适合“整文件”模式。

### `GET /docs/worker/{id}`
- 返回任务状态，`state` 取值 `PENDING` / `IN_PROGRESS` / `SUCCEEDED` / `FAILED`。
- `data` 中包含 `documentation`、`preview`、`feedbackId`、`commentFormat`、`modelProvider` 等字段。

### `POST /docs/feedback`
- `feedback` ∈ `{1, 0, -1}`，记录正负反馈。

### `POST /docs/intro`、`POST /docs/intro/discover`
- 记录问卷目的 / 渠道来源，字段分别为 `purpose`、`source`。

## 公共 API（需 `API-KEY`）

| 路径 | 说明 |
| --- | --- |
| `POST /v1/document` | 在线生成文档，结构与 Node 版本一致；每 15 分钟最多 100 次（Redis 滑动窗口限流）。 |
| `GET /v1/list/languages` | 返回 `languages: string[]`。 |
| `GET /v1/list/formats` | 返回格式数组（`id` + `defaultLanguages`）。 |

> 若未配置 `API-KEY`，Web 前端会退化为本地内置语言/格式列表，但无法调用 `/v1/document`。

## 已下线的接口

为聚焦“文档生成”主线，登录、团队协作、Playground、Stripe Webhook 等模块已在本迭代中移除。若需要这些功能，可参考仓库内保留的 `ProjectDocumentationGenerator/server` 目录自行恢复。

## 错误码

| 枚举 | 含义 |
| --- | --- |
| `VALIDATION_FAILED (40001)` | 请求参数校验失败 |
| `AUTHENTICATION_FAILED (40100)` | 缺少或错误的 API Key |
| `RATE_LIMITED (42900)` | 触发限流（Redis 滑动窗口） |
| `NOT_IMPLEMENTED (50100)` | 功能尚未实现（如 Stripe Portal 占位） |
| `INTERNAL_ERROR (50000)` | 未捕获的服务器异常 |

推荐客户端以 `code` + `message` 为准，不直接依赖 HTTP 状态码。

## 认证与用户

### `POST /auth/register`
- **说明**：创建账号并返回访问令牌。管理员账号由 `security.admin.*` 配置，普通注册者默认为 STANDARD 角色（15 分钟 100 次配额，可由管理员调整或升级为 PREMIUM）。
- **请求体**：`username`、`email`、`password`。
- **响应**：`{ token, username, role, apiQuota }`。

### `POST /auth/login`
- **说明**：用户名 + 密码登录，返回 JWT。
- **响应**：同注册接口。

所有需要登录的接口需在请求头附带 `Authorization: Bearer <token>`。

## 历史记录

### `GET /docs/history`
- **说明**：获取当前用户的文档生成历史，支持 `page`、`size`、`keyword`、`language`、`source` 查询参数。
- **权限**：登录用户可访问自己的记录；管理员可额外通过 `userId` 参数查看指定用户。
- **响应**：`PageResponse<DocHistoryItem>`，记录包含时间、语言、模型、耗时、预览等字段。

## 管理员 API

所有 `/admin/**` 接口仅管理员可调用。

### `GET /admin/users`
- 返回所有注册用户及其邮箱、角色、配额、创建时间。

### `PUT /admin/users/{id}`
- 更新邮箱、角色（`STANDARD`/`PREMIUM`/`ADMIN`）、配额（-1 表示无限制）。

### `GET /admin/api-keys`
- 列出目前存储的 API-Key（仅返回哈希值、不回显明文）。

### `POST /admin/api-keys`
- 接收 `firstName`、`lastName`、`email`、`purpose`、`rawKey`，写入数据库并返回包含哈希的记录。响应中会携带 `rawKey` 便于复制。

### `DELETE /admin/api-keys/{id}`
- 删除指定 ID 的 API-Key。
