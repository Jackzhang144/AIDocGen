# 使用指南

本指南展示如何调用 Java 版 Mintlify 后端的核心接口，并说明常见调试步骤。所有示例均基于默认的 `http://localhost:8080` 服务地址，数据采用 JSON 传输。

> 详细字段与返回值请参阅 [`API_DOCUMENTATION.md`](API_DOCUMENTATION.md)。

## 1. 文档生成流程

### 1.1 创建任务

```bash
curl -X POST http://localhost:8080/docs/write/v3 \
  -H "Content-Type: application/json" \
  -d '{
    "code": "function add(a, b) { return a + b; }",
    "languageId": "javascript",
    "userId": "demo-user",
    "email": "dev@example.com",
    "commented": true,
    "fileName": "math.ts",
    "source": "vscode"
  }'
```

响应示例：

```json
{
  "id": "21a9f5d4-5d85-4f51-990d-cd6c8357a0c9"
}
```

- `userId` 为配额统计所必需，推荐使用插件或用户的固定 UUID。
- 未登录用户 30 天内最多调用 60 次，超过后会收到 `requiresAuth` 响应。

### 1.2 轮询结果

```bash
curl http://localhost:8080/docs/worker/21a9f5d4-5d85-4f51-990d-cd6c8357a0c9
```

`state=completed` 时可在 `data.docstring` 中读取内容；若 `state=failed`，`reason` 中包含失败原因。

### 1.3 反馈与问卷

```bash
curl -X POST http://localhost:8080/docs/feedback \
  -H "Content-Type: application/json" \
  -d '{"id":"db4c2140-9320-4703-9e8d-6d9ed768d6b6","feedback":1}'
```

问卷接口：`/docs/intro`、`/docs/intro/discover`，字段与原 Mintlify 插件保持一致。

## 2. 团队协作

### 2.1 查询团队

```bash
curl "http://localhost:8080/team?email=lead@example.com"
```

返回管理员邮箱与成员列表，`invitePending=true` 表示该成员尚未注册。

### 2.2 邀请成员

```bash
curl -X POST http://localhost:8080/team/invite \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "demo-user",
    "fromEmail": "lead@example.com",
    "toEmail": "teammate@example.com",
    "shouldCreateTeam": true
  }'
```

- 发起邀请的账户需属于 Premium 计划。
- 一个团队最多两位成员（不含管理员），重复邀请会报错。

### 2.3 撤销邀请

```bash
curl -X DELETE http://localhost:8080/team/invite \
  -H "Content-Type: application/json" \
  -d '{
    "fromEmail": "lead@example.com",
    "toEmail": "teammate@example.com"
  }'
```

## 3. 用户账户与订阅

### 3.1 兑换 Auth0 授权码

```bash
curl -X POST http://localhost:8080/user/code \
  -H "Content-Type: application/json" \
  -d '{
    "code": "AUTHORIZATION_CODE",
    "userId": "demo-user",
    "uriScheme": "vscode"
  }'
```

服务端会向 Auth0 请求用户信息并写入数据库，返回用户邮箱以及当前是否升级为 Premium。

### 3.2 检查订阅状态

```bash
curl -X POST http://localhost:8080/user/status \
  -H "Content-Type: application/json" \
  -d '{"email":"dev@example.com"}'
```

可能返回：`unauthenticated`、`unaccounted`、`community`、`member`、`team`。

### 3.3 Stripe Webhook

部署线上环境时，将 Stripe 的 webhook 指向 `/webhooks/stripe`。收到结账成功 / 订阅取消事件后，服务会自动更新用户的 `plan` 字段。

## 4. 公共 API 调用

### 4.1 准备 API Key

- 管理员生成：
  ```bash
  curl -X POST http://localhost:8080/functions/api \
    -H "Content-Type: application/json" \
    -d '{
      "accessKey": "<ADMIN_ACCESS_KEY>",
      "firstName": "Mint",
      "lastName": "Dev",
      "email": "dev@example.com"
    }'
  ```
- Typeform Webhook：将表单提交地址配置为 `/functions/typeform`，服务会自动读取 `field.ref` 生成密钥。

### 4.2 调用公共接口

```bash
curl http://localhost:8080/v1/list/languages \
  -H "API-KEY: <PLAIN_KEY_FROM_ABOVE>"
```

Key 会在服务端进行 SHA-1 哈希后校验；缺失或错误会返回 401。

## 5. 管理与调试

### 5.1 Playground

```bash
curl -X POST http://localhost:8080/playground/mints/synopsis \
  -H "Content-Type: application/json" \
  -d '{
    "accessKey": "<ADMIN_ACCESS_KEY>",
    "code": "class User { }",
    "languageId": "java"
  }'
```

- `mode=ast`：返回简易抽象语法树。
- `mode=synopsis`：返回内部分析结果。

### 5.2 根路由

访问 `GET /` 可验证服务是否启动成功，期望输出 `🌳 Welcome to the Mintlify API`。

### 5.3 查看运行日志

- 本地运行时，Spring Boot 默认输出到控制台，可配合 `tail -f logs/spring.log`（若配置了文件输出）或 IDE 控制台实时查看。
- 若需定位更细粒度问题，可在 `application.yml` 中将特定组件调整为 DEBUG：

  ```yaml
  logging:
    level:
      com.codecraft.documentationgenerator.controller.DocsController: DEBUG
      com.codecraft.documentationgenerator.service.impl.DocJobService: DEBUG
  ```

- 公共 API、Webhook、文档生成等链路会自动记录受理人、操作结果与脱敏邮箱/API Key，出现 WARN 日志时可重点关注鉴权失败或必填字段缺失。

## 6. 常见问题

| 问题 | 说明与解决方案 |
| --- | --- |
| `requiresAuth` 响应 | 检查请求体是否包含 `userId`，或登录后重试 |
| AI 生成为空 | 确认 `OPENAI_API_KEY` 是否配置，或替换为自定义模型 |
| 访问 `/functions/api` 返回 `Invalid access key` | 请设置 `ADMIN_ACCESS_KEY` 并在请求体携带正确的 `accessKey` |
| `No API key provided` | 访问公共 API 时需要在请求头中显式包含 `API-KEY` |

## 7. 参考资料

- 架构与部署说明：[README.md](README.md)
- 接口字段详情：[API_DOCUMENTATION.md](API_DOCUMENTATION.md)
