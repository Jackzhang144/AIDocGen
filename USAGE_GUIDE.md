# 使用指南

本文档详细介绍了如何使用DocumentationGenerator后端服务的各项功能。

## 认证流程

### 1. 用户注册

首先需要注册一个账户。向 `/api/auth/register` 发送POST请求：

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "name": "用户名"
  }'
```

成功后会返回JWT token：

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.xxxx",
  "message": "注册成功"
}
```

### 2. 用户登录

如果已有账户，可以通过登录获取访问令牌。向 `/api/auth/login` 发送POST请求：

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

成功后会返回JWT token：

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.xxxx",
  "message": "登录成功"
}
```

### 3. 使用认证令牌

获取到JWT token后，在后续所有需要认证的请求中，都需要在HTTP请求头中添加Authorization字段：

```bash
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.xxxx
```

例如获取用户信息：

```bash
curl -X GET http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.xxxx"
```

## 错误处理

### 统一错误响应格式

所有API接口在发生错误时都返回统一的JSON格式：

```json
{
  "code": 400,
  "message": "具体的错误信息",
  "data": null
}
```

其中：
- **code**: HTTP状态码（如400表示业务错误，500表示系统错误）
- **message**: 错误描述信息
- **data**: 错误相关的附加数据（通常为null）

### 1. 认证失败 (401)

当未提供有效的JWT token或token已过期时，会返回401错误：

```json
{
  "code": 401,
  "message": "用户未登录",
  "data": null
}
```

### 2. 权限不足 (403)

当用户尝试访问无权限的资源时，会返回403错误。

### 3. 资源未找到 (404)

当请求的资源不存在时，会返回404错误：

```json
{
  "code": 404,
  "message": "文档不存在",
  "data": null
}
```

### 4. 请求参数错误 (400)

当请求参数不正确或业务逻辑出错时，会返回400错误：

```json
{
  "code": 400,
  "message": "用户不存在",
  "data": null
}
```

### 5. 系统内部错误 (500)

当系统发生未预期的错误时，会返回500错误：

```json
{
  "code": 500,
  "message": "服务器异常",
  "data": null
}
```

## 最佳实践

### 1. 安全性

- 始终使用HTTPS传输数据
- 不要在客户端存储明文密码
- 定期刷新JWT token
- 限制API密钥的使用范围

### 2. 性能优化

- 合理使用分页获取大量数据
- 避免不必要的API调用
- 使用缓存机制存储频繁访问的数据

### 3. 错误处理

- 始终检查API响应状态码
- 记录错误日志以便排查问题
- 向用户提供友好的错误提示

### 4. 日志记录

- 在关键业务逻辑处添加适当的日志记录
- 区分不同级别的日志（INFO, WARN, ERROR）
- 避免记录敏感信息（如密码、密钥等）