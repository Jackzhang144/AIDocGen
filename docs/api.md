# 接口说明

后端服务统一挂载在 `http://<host>:8080/api`。除明确说明外，所有接口都返回如下结构：

```json
{
  "code": 0,
  "message": "操作描述",
  "data": {},
  "success": true
}
```

- `code = 0` 表示成功，其余取值见「错误码」。
- HTTP 状态码与 `code` 保持一致，客户端以 `code` + `message` 为准。

## 鉴权与上下文

| 场景 | 方式 | 说明 |
| --- | --- | --- |
| `/docs/**`、`/admin/**` | `Authorization: Bearer <JWT>` | 登录或注册后返回 `token`，JWT 有效期由 `security.jwt.expiration-ms` 决定。 |
| `/api/v1/**` | `API-KEY: <raw>` | 后端会对原始 Key 做 SHA-1 哈希并与 `api_keys.hashed_key` 比较。 |
| `/auth/**`、`/`, `/health`, `/actuator/**` | 无 | 公开访问，用于登录/探活。 |

跨域白名单位于 `web.cors.allowed-origins`（默认 `http://localhost:5173`）。
> 所有上述配置均位于 `src/main/resources/application-example.yml` 模板中；复制为 `application.yml` 后即可按环境覆盖，而不会将真实密钥提交到 Git。

### 速率限制
- **异步生成接口**：STANDARD 用户每 15 分钟默认 100 次（`DocController.USER_RATE_WINDOW_SECONDS`），PREMIUM/ADMIN 不限；配额可在管理员面板调整，`-1` 代表关闭限制。
- **公共 API `/api/v1/document`**：任意 `API-KEY` 每 15 分钟 100 次，由 `RateLimiterService`（Redis → 内存降级）实现。

## 常用枚举与结构

### 语言标识

| ID | 说明 |
| --- | --- |
| `python` | Python |
| `javascript` / `javascriptreact` | JS / React |
| `typescript` / `typescriptreact` | TS / React |
| `java` / `kotlin` | JVM |
| `c` / `cpp` | C/C++ |
| `php` | PHP |
| `csharp` | C# |
| `ruby` | Ruby |
| `go` | Go |
| `rust` | Rust |

前端在 `DocGenerator.vue` 内维护可选项，若传入未支持的 ID 会触发 `VALIDATION_FAILED`。

### 文档格式 (`docStyle`)

| ID | 适用语言（默认） |
| --- | --- |
| `Auto-detect` | 自动推断（默认） |
| `JSDoc` | JS/TS、Java/Kotlin |
| `Javadoc` | Java/Kotlin |
| `DocBlock` / `Doxygen` | PHP / C / C++ |
| `Google` | Python / Go |
| `reST` / `NumPy` | Python |

### 任务状态 (`JobState`)

`PENDING` → `IN_PROGRESS` → `SUCCEEDED` / `FAILED`。`reason` 字段仅在失败时存在。

### 分页结构 (`PageResponse`)

```json
"data": {
  "total": 25,
  "page": 1,
  "size": 5,
  "records": []
}
```

## 文档生成（异步任务）

所有 `/docs/**` 接口均需有效 JWT。

### `POST /docs/write/v3`
- **用途**：提交“选区”模式任务（`isSelection=true`）。
- **请求体字段**：

| 字段 | 说明 |
| --- | --- |
| `code` | 选中的代码片段。`code` 与 `context` 至少填一个。 |
| `context` | 额外上下文（整文件模式使用）。 |
| `languageId`* | 语言标识，见上表。 |
| `commented` | `true` 时在服务端套用注释包裹。 |
| `docStyle` | 文档格式 ID（传空自动检测）。 |
| `width` | 推荐换行宽度（列数）。 |
| `isSelection` | 前端可显式写入，但会被服务端强制为 `true`。 |
| 其它 | `mode`、`fileName`、`location`、`line`、`source` 将原样记录入库。 |

- **响应**：`{ "data": { "id": "<job-id>" } }`

### `POST /docs/write/v3/no-selection`
- **说明**：与 `/docs/write/v3` 相同，但 `isSelection=false`，适合“整文件解析后再提取函数”的场景。

### `GET /docs/worker/{id}`
- **说明**：轮询任务状态。返回结构：

```json
{
  "data": {
    "id": "job-uuid",
    "state": "IN_PROGRESS",
    "reason": null,
    "data": {
      "documentation": "...",
      "preview": "...",
      "feedbackId": "fb-uuid",
      "docFormat": "JSDoc",
      "commentFormat": "JSDOC",
      "modelProvider": "openai",
      "position": "Above",
      "cursorMarker": null,
      "inferenceLatencyMs": 1850
    }
  }
}
```

成功后可使用 `feedbackId` 调用反馈接口。

### `POST /docs/feedback`
- **请求体**：`{ "id": "<feedbackId>", "feedback": 1 | 0 | -1 }`
- **作用**：写入 `docs.feedback` 字段并产生日志埋点。

### `POST /docs/intro` 与 `POST /docs/intro/discover`
- 绑定 `feedbackId` 记录问卷目的（`purpose`）与渠道来源（`source`），用于后续画像。

## 历史记录

### `GET /docs/history`
- **权限**：登录用户；若角色 ≠ ADMIN，自动仅返回本人记录（服务端会覆盖 `userId`）。
- **查询参数**：

| 参数 | 说明 |
| --- | --- |
| `page` / `size` | 分页（默认 1 / 10，`size` ∈ [1, 100]）。 |
| `keyword` | 模糊匹配 `output/prompt/feedbackId`。 |
| `language`、`source` | 精确过滤。 |
| `userId` | 仅管理员可生效，用于查看任意账号。 |

- **记录字段**：`timestamp`、`language`、`source`、`timeToGenerate`、`selection`、`feedback`、`modelProvider`、`outputPreview`、`promptPreview` 等，详见 `DocHistoryItem`。

## 管理端接口（需 ADMIN 角色）

### 用户管理
- `GET /admin/users`：返回所有用户的 `id/username/email/role/apiQuota/createdAt`。
- `PUT /admin/users/{id}`：请求体 `{ "email": "", "role": "STANDARD|PREMIUM|ADMIN", "apiQuota": -1 }`，用于调配配额与角色。

### API Key 管理
- `GET /admin/api-keys`：列出 `api_keys` 表的所有记录。
- `POST /admin/api-keys`：请求体：

| 字段 | 说明 |
| --- | --- |
| `firstName` / `lastName` | 联系人。 |
| `email` | 便于追溯。 |
| `purpose` | 使用描述。 |
| `rawKey` | 原始 Key，会被 SHA-1 后保存；响应会回显 `rawKey` 方便一次性展示。 |

- `DELETE /admin/api-keys/{id}`：删除指定 Key。

## 认证接口

### `POST /auth/register`
- **请求体**：`{ "username": "...", "email": "user@example.com", "password": "******" }`
- **响应**：`AuthResponse`，包含 `token/username/email/role/apiQuota`。普通注册者默认为 `STANDARD`，配额 100。

### `POST /auth/login`
- **说明**：用户名 + 密码登录，成功后返回同 `AuthResponse`，前端会将 `token` 保存至 `localStorage`。

管理员账号由 `security.admin.username/password/email` 注入（`AdminAccountInitializer` 在启动时自动确保存在，默认配额 `-1`）。

## 公共 API（需 `API-KEY`）

所有请求位于 `/api/v1/**`，需通过 `API-KEY` 请求头完成鉴权。

| 路径 | 说明 |
| --- | --- |
| `POST /v1/document` | 同步生成文档。请求体遵循 `DocumentGenerationRequest`（`code`、`language`、`commented`、`format`、`context`、`width`），响应 `{ "documentation": "..." }`。 |
| `GET /v1/list/languages` | 返回 `{ "languages": string[] }`，由 `LanguageId.publicLanguageIds()` 提供。 |
| `GET /v1/list/formats` | 返回 `{ "formats": [{ "id": "JSDoc", "defaultLanguages": [...] }] }`，会过滤掉 `AUTO_DETECT`。 |

未携带或提供错误 `API-KEY` 会返回 `AUTHENTICATION_FAILED`；超出 15 分钟 100 次配额会收到 `RATE_LIMITED`。

## 其他辅助接口
- `GET /`、`GET /health`：返回 `Aidoc backend is running`，用于探活。
- `GET /actuator/**`：Spring Boot Actuator 自监控接口。
- `GET /v3/api-docs`、`/swagger-ui/**`、`/doc.html`：OpenAPI/Knife4j 文档。

## 错误码

| 枚举 | HTTP | 含义 |
| --- | --- | --- |
| `VALIDATION_FAILED` | 400 | 请求参数缺失或非法。 |
| `AUTHENTICATION_FAILED` | 401 | JWT 或 API-KEY 无效。 |
| `AUTHORIZATION_FAILED` | 403 | 当前用户无权访问目标接口。 |
| `RESOURCE_NOT_FOUND` | 404 | 任务、用户等资源不存在。 |
| `RATE_LIMITED` | 429 | 触发滑动窗口限流。 |
| `DUPLICATE_RESOURCE` | 409 | 资源重复，例如用户名、API Key 等。 |
| `UPSTREAM_FAILURE` | 502 | 模型网关或外部依赖失败。 |
| `NOT_IMPLEMENTED` | 501 | 占位功能尚未开放。 |
| `INTERNAL_ERROR` | 500 | 未捕获的服务器异常。 |

所有错误同样使用 `ApiResponse` 包裹，并携带 `success=false`。
