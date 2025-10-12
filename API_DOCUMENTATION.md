# API接口文档

本文档详细描述了DocumentationGenerator后端服务的所有API接口。

## 基础信息

- **基础URL**: `http://localhost:8080/api/`
- **认证方式**: JWT Token
- **数据格式**: JSON

## 认证说明

除了认证相关接口外，所有其他接口都需要在HTTP请求头中添加Authorization字段：

```
Authorization: Bearer your_jwt_token_here
```

## 错误响应格式

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

## 认证相关接口

### 用户注册

- **URL**: `POST /api/auth/register`
- **描述**: 用户注册新账户
- **请求参数**:
  ```json
  {
    "email": "user@example.com",
    "password": "password123",
    "name": "用户名"
  }
  ```
- **响应**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9.xxxx",
    "message": "注册成功"
  }
  ```

### 用户登录

- **URL**: `POST /api/auth/login`
- **描述**: 用户登录获取访问令牌
- **请求参数**:
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```
- **响应**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9.xxxx",
    "message": "登录成功"
  }
  ```

## 用户管理接口

### 获取用户信息

- **URL**: `GET /api/users/{id}`
- **描述**: 根据用户ID获取用户详细信息
- **权限**: 需要登录
- **路径参数**:
    - `id`: 用户ID
- **响应**:
  ```json
  {
    "id": 1,
    "email": "user@example.com",
    "name": "用户名",
    "password": "$2a$10$xxxx",
    "createdAt": "2023-01-01T00:00:00",
    "lastLoginAt": "2023-01-01T00:00:00",
    "refreshToken": "xxxx",
    "plan": "free",
    "stripeCustomerId": null,
    "updatedAt": "2023-01-01T00:00:00"
  }
  ```

### 根据邮箱获取用户信息

- **URL**: `GET /api/users/email/{email}`
- **描述**: 根据用户邮箱获取用户详细信息
- **权限**: 需要登录
- **路径参数**:
    - `email`: 用户邮箱
- **响应**: 用户对象

### 创建用户

- **URL**: `POST /api/users`
- **描述**: 创建新用户
- **权限**: 需要登录
- **请求参数**: 用户对象
- **响应**: 无

### 更新登录信息

- **URL**: `PUT /api/users/login`
- **描述**: 更新用户登录相关信息
- **权限**: 需要登录
- **请求参数**: 用户对象
- **响应**: 无

### 更新订阅信息

- **URL**: `PUT /api/users/subscription`
- **描述**: 更新用户订阅相关信息
- **权限**: 需要登录
- **请求参数**: 用户对象
- **响应**: 无

### 删除用户

- **URL**: `DELETE /api/users/{id}`
- **描述**: 根据用户ID删除用户
- **权限**: 需要登录
- **路径参数**:
    - `id`: 用户ID
- **响应**: 无

### 获取所有用户

- **URL**: `GET /api/users`
- **描述**: 获取所有用户列表
- **权限**: 需要登录
- **响应**: 用户对象列表

## 文档管理接口

### 获取文档信息

- **URL**: `GET /api/docs/{id}`
- **描述**: 根据文档ID获取文档详细信息
- **权限**: 需要登录
- **路径参数**:
    - `id`: 文档ID
- **响应**: 文档对象

### 根据用户ID获取文档列表

- **URL**: `GET /api/docs/user/{userId}`
- **描述**: 根据用户ID获取该用户的所有文档
- **权限**: 需要登录
- **路径参数**:
    - `userId`: 用户ID
- **响应**: 文档对象列表

### 根据反馈ID获取文档

- **URL**: `GET /api/docs/feedback/{feedbackId}`
- **描述**: 根据反馈ID获取文档
- **权限**: 需要登录
- **路径参数**:
    - `feedbackId`: 反馈ID
- **响应**: 文档对象

### 创建文档

- **URL**: `POST /api/docs`
- **描述**: 创建新文档
- **权限**: 需要登录
- **请求参数**: 文档对象
- **响应**: 无

### 更新文档反馈

- **URL**: `PUT /api/docs/feedback`
- **描述**: 更新文档反馈信息
- **权限**: 需要登录
- **请求参数**: 文档对象
- **响应**: 无

### 删除文档

- **URL**: `DELETE /api/docs/{id}`
- **描述**: 根据文档ID删除文档
- **权限**: 需要登录
- **路径参数**:
    - `id`: 文档ID
- **响应**: 无

### 获取所有文档

- **URL**: `GET /api/docs`
- **描述**: 获取所有文档列表
- **权限**: 需要登录
- **响应**: 文档对象列表

## API密钥管理接口

### 获取API密钥信息

- **URL**: `GET /api/apikeys/{id}`
- **描述**: 根据ID获取API密钥详细信息
- **权限**: 需要登录
- **路径参数**:
    - `id`: API密钥ID
- **响应**: API密钥对象

### 根据哈希值获取API密钥

- **URL**: `GET /api/apikeys/hashed/{hashedKey}`
- **描述**: 根据哈希值获取API密钥
- **权限**: 需要登录
- **路径参数**:
    - `hashedKey`: 哈希后的API密钥
- **响应**: API密钥对象

### 创建API密钥

- **URL**: `POST /api/apikeys`
- **描述**: 创建新的API密钥
- **权限**: 需要登录
- **请求参数**: API密钥对象
- **响应**: 无

### 删除API密钥

- **URL**: `DELETE /api/apikeys/{id}`
- **描述**: 根据ID删除API密钥
- **权限**: 需要登录
- **路径参数**:
    - `id`: API密钥ID
- **响应**: 无

### 获取所有API密钥

- **URL**: `GET /api/apikeys`
- **描述**: 获取所有API密钥列表
- **权限**: 需要登录
- **响应**: API密钥对象列表

## 团队管理接口

### 获取团队信息

- **URL**: `GET /api/teams/{id}`
- **描述**: 根据ID获取团队详细信息
- **权限**: 需要登录
- **路径参数**:
    - `id`: 团队ID
- **响应**: 团队对象

### 根据管理员邮箱获取团队

- **URL**: `GET /api/teams/admin/{admin}`
- **描述**: 根据管理员邮箱获取团队信息
- **权限**: 需要登录
- **路径参数**:
    - `admin`: 管理员邮箱
- **响应**: 团队对象

### 创建团队

- **URL**: `POST /api/teams`
- **描述**: 创建新团队
- **权限**: 需要登录
- **请求参数**: 团队请求对象
- **响应**: 无

### 更新团队成员

- **URL**: `PUT /api/teams/members`
- **描述**: 更新团队成员列表
- **权限**: 需要登录
- **请求参数**: 团队请求对象
- **响应**: 无

### 删除团队

- **URL**: `DELETE /api/teams/{id}`
- **描述**: 根据ID删除团队
- **权限**: 需要登录
- **路径参数**:
    - `id`: 团队ID
- **响应**: 无

### 获取所有团队

- **URL**: `GET /api/teams`
- **描述**: 获取所有团队列表
- **权限**: 需要登录
- **响应**: 团队对象列表

## 文档生成接口

### 生成文档

- **URL**: `POST /api/writer/write/v3`
- **描述**: 根据提供的代码生成文档
- **权限**: 需要登录
- **请求参数**:
  ```json
  {
    "code": "function example() { return 'Hello World'; }",
    "languageId": "javascript",
    "fileName": "example.js",
    "context": "上下文信息",
    "location": 10,
    "line": "function example() {",
    "isSelection": true,
    "docFormat": "jsdoc"
  }
  ```
- **响应**: 生成的文档字符串

### 无选择代码生成文档

- **URL**: `POST /api/writer/write/v3/no-selection`
- **描述**: 根据上下文信息提取代码并生成文档
- **权限**: 需要登录
- **请求参数**: 生成文档请求对象
- **响应**: 生成的文档字符串