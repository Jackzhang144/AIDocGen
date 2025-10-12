# DocumentationGenerator

AI驱动的代码文档生成器后端服务，基于Spring Boot 3、MySQL和MyBatis构建。

## 功能特性

- 用户管理（注册、登录、订阅）
- 文档生成（使用AI自动生成代码文档）
- API密钥管理
- 团队协作功能
- 多语言代码支持
- 基于JWT的用户认证和授权
- AOP切面编程实现权限控制
- 全局异常处理机制

## 技术栈

- **后端框架**: Spring Boot 3
- **数据库**: MySQL + MyBatis
- **AI集成**: Spring AI + OpenAI
- **安全框架**: Spring Security + JWT
- **API文档**: Knife4j（中文界面）
- **切面编程**: Spring AOP
- **测试**: JUnit 5 + Mockito

## 环境要求

- Java 17+
- MySQL 8.0+
- Maven 3.6+ (或使用Maven Wrapper)

## 快速开始

### 1. 数据库设置

```sql
CREATE DATABASE doc_generator CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后执行 `src/main/resources/schema.sql` 中的SQL脚本创建表结构。

### 2. 配置环境变量

在 `src/main/resources/application.yml` 中配置：

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/doc_generator?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8
    username: your_username
    password: your_password
# OpenAI配置（可选，用于文档生成功能）
spring:
  ai:
    openai:
      api-key: your_openai_api_key
# JWT密钥配置（建议在生产环境中修改为安全的密钥）
jwt:
  secret: your_jwt_secret_key
```

### 3. 构建和运行

使用Maven构建项目：

```bash
# 如果系统中安装了Maven
mvn spring-boot:run

# 或者使用Maven Wrapper（如果项目中有mvnw脚本）
./mvnw spring-boot:run
```

### 4. 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn -Dtest=UserServiceIntegrationTest test
```

## API接口文档

详细接口文档请参考 [接口文档](./API_DOCUMENTATION.md) 文件。

## 使用方法

详细使用方法请参考 [使用指南](./USAGE_GUIDE.md) 文件。

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/codecraft/documentationgenerator/
│   │       ├── aop/              # AOP切面编程相关
│   │       ├── config/           # 配置类
│   │       │   └── security/     # 安全配置
│   │       ├── controller/       # 控制器层
│   │       ├── entity/           # 实体类
│   │       ├── mapper/           # MyBatis映射器
│   │       ├── model/            # 数据传输对象
│   │       ├── service/          # 业务逻辑层接口
│   │       │   └── impl/         # 业务逻辑层实现
│   │       ├── util/             # 工具类
│   │       └── DocumentationGeneratorApplication.java  # 应用入口
│   └── resources/
│       ├── mapper/               # MyBatis XML映射文件
│       ├── application.yml       # 配置文件
│       └── schema.sql            # 数据库脚本
└── test/                         # 测试代码
```

## 核心模块说明

### 1. 用户管理模块

- 用户注册、登录和信息管理
- 订阅计划管理
- API密钥管理
- 基于JWT的用户认证
- 使用Spring Security进行安全控制

### 2. 文档生成模块

- 基于AI的代码文档自动生成
- 支持多种编程语言
- 文档反馈和评分机制

### 3. 团队协作模块

- 团队创建和管理
- 成员邀请和权限管理

### 4. 安全认证模块

- 基于JWT的用户认证机制
- 使用Spring Security进行权限控制
- 自定义AOP注解[@RequireLogin](file:///Users/jackzhang/Code/IdeaProjects/DocumentationGenerator_Back/src/main/java/com/codecraft/documentationgenerator/aop/RequireLogin.java)实现方法级权限控制
- 密码使用BCrypt加密存储

### 5. 全局异常处理

项目实现了全局异常处理机制，确保：

1. **统一异常响应格式**
    - 所有异常都返回统一的JSON格式：`{code: 状态码, message: 错误信息, data: null}`
    - 业务异常返回400状态码和具体错误信息
    - 系统异常返回500状态码和通用错误信息

2. **安全性**
    - 防止敏感系统信息泄露给前端
    - 对外只暴露必要的错误信息

3. **日志记录**
    - 详细记录异常信息，便于问题排查
    - 业务异常记录具体消息内容
    - 系统异常记录完整堆栈信息

## 数据库设计

### 主要数据表

1. **用户表 (users)**
    - 存储用户基本信息和订阅状态

2. **文档表 (docs)**
    - 存储生成的文档内容和相关信息

3. **API密钥表 (api_keys)**
    - 管理用户的API访问密钥

4. **团队表 (teams)**
    - 存储团队信息和成员列表

### 团队表 (teams) 成员字段处理

团队表中的 `members` 字段使用 MySQL 的 JSON 类型存储成员信息。在 Java 代码中，我们将成员数组序列化为 JSON
字符串进行存储，在读取时再解析为数组。

示例：

```json
[
  "member1@example.com",
  "member2@example.com"
]
```

## 开发指南

### 代码规范

1. 使用Lombok简化实体类代码
2. 遵循RESTful API设计原则
3. 使用JavaDoc为所有公共方法添加注释
4. 保持控制器层轻量，业务逻辑放在服务层
5. 服务层采用接口+实现类的设计模式
6. 使用自定义注解和AOP实现横切关注点

### 安全设计

1. **JWT认证**：用户登录后生成JWT token，后续请求需在Header中携带token
2. **方法级权限控制**：通过[@RequireLogin](file:///Users/jackzhang/Code/IdeaProjects/DocumentationGenerator_Back/src/main/java/com/codecraft/documentationgenerator/aop/RequireLogin.java)注解保护敏感接口
3. **密码加密**：用户密码使用BCrypt强加密算法存储
4. **SQL注入防护**：使用MyBatis参数化查询防止SQL注入

### API文档

本项目使用Knife4j作为API文档工具，提供中文界面和增强功能：

- 访问地址：http://localhost:8080/doc.html
- 支持在线测试API接口
- 提供详细的接口说明和参数描述

### AOP切面编程

项目使用Spring AOP实现横切关注点的统一处理：

1. **权限检查切面**：[@RequireLogin](file:///Users/jackzhang/Code/IdeaProjects/DocumentationGenerator_Back/src/main/java/com/codecraft/documentationgenerator/aop/RequireLogin.java)注解用于保护需要认证的接口
2. 所有被[@RequireLogin](file:///Users/jackzhang/Code/IdeaProjects/DocumentationGenerator_Back/src/main/java/com/codecraft/documentationgenerator/aop/RequireLogin.java)注解标记的方法在执行前都会先检查用户是否已登录
3. 未登录用户访问受保护接口时会返回401未授权错误

### 全局异常处理

项目实现了全局异常处理机制，确保：

1. **统一异常响应格式**
    - 所有异常都返回统一的JSON格式：`{code: 状态码, message: 错误信息, data: null}`
    - 业务异常返回400状态码和具体错误信息
    - 系统异常返回500状态码和通用错误信息

2. **安全性**
    - 防止敏感系统信息泄露给前端
    - 对外只暴露必要的错误信息

3. **日志记录**
    - 详细记录异常信息，便于问题排查
    - 业务异常记录具体消息内容
    - 系统异常记录完整堆栈信息

### 测试策略

项目采用分层测试策略：

1. **单元测试**
    - 针对服务层逻辑进行测试
    - 使用Mockito模拟依赖组件
    - 包含对异常处理机制的专门测试

2. **集成测试**
    - 测试数据库操作和业务流程
    - 使用真实的数据库环境
    - 包含Web层异常处理的集成测试

3. **控制器测试**
    - 验证API端点的正确性
    - 测试请求和响应处理
    - 验证全局异常处理在Web层的正确性

### 部署说明

1. 构建项目：
   ```bash
   mvn clean package
   ```

2. 运行应用：
   ```bash
   java -jar target/DocumentationGenerator-0.0.1-SNAPSHOT.jar
   ```

3. 确保MySQL数据库服务正在运行并正确配置

## 常见问题

### 1. 启动时报数据库连接错误

请检查application.yml中的数据库配置是否正确，确保MySQL服务正在运行。

### 2. API文档无法访问

确认应用已成功启动，访问 http://localhost:8080/doc.html 查看API文档。

### 3. AI文档生成功能不可用

检查是否正确配置了OpenAI API密钥，该功能需要有效的API密钥才能工作。

### 4. 访问受保护接口返回401错误

确保已通过`/api/auth/login`接口登录并获取JWT token，并在请求Header中正确设置：

```
Authorization: Bearer your_jwt_token
```