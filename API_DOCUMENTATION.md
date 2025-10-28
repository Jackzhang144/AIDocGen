# 文档生成后端接口说明

本文档汇总了基于 Java 重写的 Mintlify 后端所有公开 HTTP 接口。接口路径、请求参数与响应结构与原 Node.js 版本保持一致，以便已有插件与调用方平滑迁移。

## 基本信息

- **基础地址**：`http://localhost:8080`
- **默认数据格式**：`application/json; charset=utf-8`
- **字符编码**：UTF-8

### 认证与头信息

| 作用范围 | 认证方式 | 说明 |
| --- | --- | --- |
| 团队 / 管理工具（`/functions/*`、`/playground/*`） | 请求体字段 `accessKey` | 必须与环境变量 `ADMIN_ACCESS_KEY` 保持一致 |
| 公共 API（`/v1/*`） | 请求头 `API-KEY` | 明文密钥会被 SHA-1 哈希后与数据库中存储的值比对 |
| 文档生成队列（`/docs/*`） | 请求体字段 `userId` | 用于配额统计，缺失时请求会被拒绝 |

> `/api/*` 下的旧式 JWT 接口仍然保留，但表格中的 Mintlify 兼容接口无需 JWT。

### 错误响应

除特别说明外，错误会返回以下结构：

```json
{
  "code": 400,
  "message": "描述错误的信息",
  "data": null
}
```

公共 API 鉴权失败返回：

```json
{
  "error": "No API key provided"
}
```

当文档配额耗尽时，返回：

```json
{
  "requiresAuth": true,
  "message": "Please sign in to continue. By doing so, you agree to Mintlify's terms and conditions",
  "button": "🔐 Sign in",
  "error": "Please update the extension to continue"
}
```

---

## 文档生成接口（`/docs`）

### `POST /docs/write/v3`
根据选中的代码片段创建异步文档生成任务。

**请求示例**

```json
{
  "code": "function add(a, b) { return a + b; }",
  "languageId": "javascript",
  "fileName": "math.ts",
  "context": "可选上下文",
  "email": "dev@example.com",
  "userId": "client-generated-id",
  "commented": true,
  "docStyle": "Auto-detect",
  "width": 80,
  "source": "vscode"
}
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `code` | string | 是 | 待生成文档的代码片段 |
| `languageId` | string | 是 | VS Code 语言标识符 |
| `userId` | string | 是 | 用户/设备唯一标识，用于限流 |
| `email` | string | 否 | 若已登录，可帮助关联 Auth0 用户 |
| `commented` | boolean | 否 | `true` 时输出将自动包裹成注释 |
| `docStyle`/`docFormat` | string | 否 | 文档风格提示，例如 `Google`、`ReST` |
| `context`、`fileName`、`width`、`source`、`custom` | 可选 | 兼容历史参数 |

**响应**

```json
{
  "id": "21a9f5d4-5d85-4f51-990d-cd6c8357a0c9"
}
```

使用返回的 `id` 轮询 `/docs/worker/{id}` 即可获得结果。

### `POST /docs/write/v3/no-selection`
适用于未明确选择代码时，通过 `context`+`location`+`line` 自动截取片段，其他字段与 `/docs/write/v3` 保持一致。

新增字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `location` | integer | 可选，0 起始的行号提示 |
| `line` | string | 可选，当前光标所在行 |

### `GET /docs/worker/{id}`
查询文档生成任务状态。

```json
{
  "id": "21a9f5d4-5d85-4f51-990d-cd6c8357a0c9",
  "state": "completed",
  "data": {
    "docstring": "/** Adds two numbers */",
    "preview": "Adds two numbers.",
    "position": "Above",
    "feedbackId": "db4c2140-9320-4703-9e8d-6d9ed768d6b6",
    "shouldShowFeedback": true,
    "shouldShowShare": true
  }
}
```

`state` 的可能取值：`queued` → `active` → `completed`/`failed`。失败时额外包含 `reason`。

### `POST /docs/feedback`
提交指定文档的评分。

```json
{
  "id": "db4c2140-9320-4703-9e8d-6d9ed768d6b6",
  "feedback": 1
}
```

`feedback` 取值 `1` 表示好评，`-1` 表示差评。

### `POST /docs/intro`
### `POST /docs/intro/discover`
采集 IDE 首次使用问卷与渠道信息。示例：

```json
{
  "id": "db4c2140-9320-4703-9e8d-6d9ed768d6b6",
  "purpose": "Work project"
}
```

```json
{
  "id": "db4c2140-9320-4703-9e8d-6d9ed768d6b6",
  "source": "Twitter"
}
```

接口始终返回 200。

---

## 代码覆盖进度（`/progress`）

### `POST /progress`
估算文件中包含的函数、类等结构数量。

```json
{
  "file": "export function add(a, b) {\n  return a + b;\n}\n",
  "languageId": "typescript",
  "types": ["Functions", "Classes"]
}
```

**响应**

```json
{
  "current": 0,
  "total": 1,
  "breakdown": {
    "Functions": { "current": 0, "total": 1 },
    "Classes": { "current": 0, "total": 0 }
  }
}
```

`current` 表示已完成文档化的数量（当前实现固定为 0）。

---

## 团队协作（`/team`）

### `GET /team?email={email}`
按管理员或成员邮箱返回团队信息。

```json
{
  "admin": "lead@example.com",
  "members": [
    { "email": "dev@example.com", "invitePending": false },
    { "email": "new-hire@example.com", "invitePending": true }
  ]
}
```

若不存在团队，则返回 `admin` 为查询邮箱、`members` 为空数组。

### `POST /team/invite`
发送团队邀请。

```json
{
  "userId": "client-generated-id",
  "fromEmail": "lead@example.com",
  "toEmail": "new-hire@example.com",
  "shouldCreateTeam": true
}
```

约束：

- 不能邀请自己。
- `shouldCreateTeam=true` 时必须为 `premium` 付费计划。
- 一个团队最多可包含管理员 + 2 位成员。

### `DELETE /team/invite`
撤销邀请或移除成员。

```json
{
  "fromEmail": "lead@example.com",
  "toEmail": "new-hire@example.com"
}
```

---

## 用户集成（`/user`）

### `POST /user/code`
兑换 Auth0 授权码并写入用户信息。

```json
{
  "code": "AUTHORIZATION_CODE",
  "userId": "client-generated-id",
  "uriScheme": "vscode"
}
```

**响应**

```json
{
  "email": "dev@example.com",
  "isUpgraded": false
}
```

需要配置环境变量 `AUTH0_ISSUER_BASE_URL`、`AUTH0_CLIENT_ID`、`AUTH0_CLIENT_SECRET`。

### `GET /user/checkout`
### `GET /user/portal`
跳转至 Stripe 结账 / 客户中心。若未配置 Stripe，默认跳转至 Mintlify 官网。

### `POST /user/status`
根据邮箱判断账号状态（未登录、未注册、普通成员、团队管理员等）。

```json
{
  "email": "dev@example.com"
}
```

返回值：`unauthenticated`、`unaccounted`、`community`、`member`、`team`。

---

## 函数 & API Key 工具（`/functions`）

### `POST /functions/typeform`
Typeform Webhook。根据表单 `field.ref` 填写的字段生成 API Key 并持久化（仅在哈希后保存）。成功返回 200。

### `POST /functions/api`
管理员手动生成 API Key。

```json
{
  "accessKey": "<ADMIN_ACCESS_KEY>",
  "firstName": "Mint",
  "lastName": "Dev",
  "email": "dev@example.com"
}
```

**响应**

```json
{
  "key": "f96a05a4-1085-4aba-8688-7f541c29c4b6"
}
```

---

## Playground 调试（`/playground`）

### `POST /playground/mints/{mode}`
管理员调试接口。

```json
{
  "accessKey": "<ADMIN_ACCESS_KEY>",
  "code": "class User { }",
  "languageId": "java",
  "context": "optional"
}
```

- `mode=ast`：返回按行拆分的简易 AST。
- `mode=synopsis`：返回内部使用的 Synopsis 结构。

---

## 公共 API（`/v1`）

需要在请求头中携带合法的 `API-KEY`。

### `POST /v1/document`
当前处于关闭状态，固定返回：

```json
{
  "error": "The Mintlify API is currently being updated. Please email hi@mintlify for urgent authorization"
}
```

### `GET /v1/list/languages`
```json
{
  "languages": ["python", "javascript", "typescript", "javascriptreact", "typescriptreact", "php", "c", "cpp"]
}
```

### `GET /v1/list/formats`
```json
{
  "formats": [
    { "id": "JSDoc", "defaultLanguages": ["javascript", "typescript", "javascriptreact", "typescriptreact"] },
    { "id": "ReST", "defaultLanguages": ["python"] },
    { "id": "DocBlock", "defaultLanguages": ["php", "c", "cpp"] },
    { "id": "Google", "defaultLanguages": [] }
  ]
}
```

---

## Webhook（`/webhooks`）

### `POST /webhooks/stripe`
接收 Stripe 事件：`checkout.session.completed`、`invoice.paid`、`customer.subscription.deleted`、`invoice.payment_failed`。

- 成功支付：将用户 `plan` 更新为 `premium`，并保存 `stripeCustomerId`。
- 取消订阅 / 支付失败：清除付费计划。

---

## 根路由

### `GET /`
返回欢迎语：`🌳 Welcome to the Mintlify API`。

---

## 限制与提示

- 未登录用户每 30 天最多生成 60 次文档，超过后会收到 `requiresAuth` 响应。
- 所有生成结果会持久化到 `docs` 表，包含语言、注释形式、耗时指标与反馈编号等。
- 所有任务、反馈 ID 均为 UUID 字符串。

