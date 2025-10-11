# DocumentationGenerator

AI驱动的代码文档生成器后端服务，基于Spring Boot 3、MySQL和MyBatis构建。

## 功能特性

- 用户管理（注册、登录、订阅）
- 文档生成（使用AI自动生成代码文档）
- API密钥管理
- 团队协作功能
- 多语言代码支持

## 技术栈

- **后端框架**: Spring Boot 3
- **数据库**: MySQL + MyBatis
- **AI集成**: Spring AI + OpenAI
- **API文档**: Knife4j（中文界面）
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

在 `src/main/resources/application.properties` 中配置：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/doc_generator?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8
spring.datasource.username=your_username
spring.datasource.password=your_password

# OpenAI配置（可选，用于文档生成功能）
spring.ai.openai.api-key=your_openai_api_key
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

## API接口

启动服务后，可以通过以下URL访问：

- API端点: http://localhost:8080/api/
- API文档 (Knife4j中文界面): http://localhost:8080/doc.html

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/codecraft/documentationgenerator/
│   │       ├── config/           # 配置类
│   │       ├── controller/       # 控制器层
│   │       ├── entity/           # 实体类
│   │       ├── mapper/           # MyBatis映射器
│   │       ├── model/            # 数据传输对象
│   │       ├── service/          # 业务逻辑层
│   │       └── DocumentationGeneratorApplication.java  # 应用入口
│   └── resources/
│       ├── mapper/               # MyBatis XML映射文件
│       ├── application.properties # 配置文件
│       └── schema.sql            # 数据库脚本
└── test/                         # 测试代码
```

## 核心模块说明

### 1. 用户管理模块
- 用户注册、登录和信息管理
- 订阅计划管理
- API密钥管理

### 2. 文档生成模块
- 基于AI的代码文档自动生成
- 支持多种编程语言
- 文档反馈和评分机制

### 3. 团队协作模块
- 团队创建和管理
- 成员邀请和权限管理

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

团队表中的 `members` 字段使用 MySQL 的 JSON 类型存储成员信息。在 Java 代码中，我们将成员数组序列化为 JSON 字符串进行存储，在读取时再解析为数组。

示例：
```json
["member1@example.com", "member2@example.com"]
```

## 开发指南

### 代码规范

1. 使用Lombok简化实体类代码
2. 遵循RESTful API设计原则
3. 使用JavaDoc为所有公共方法添加注释
4. 保持控制器层轻量，业务逻辑放在服务层

### API文档

本项目使用Knife4j作为API文档工具，提供中文界面和增强功能：
- 访问地址：http://localhost:8080/doc.html
- 支持在线测试API接口
- 提供详细的接口说明和参数描述

### 测试策略

项目采用分层测试策略：

1. **单元测试**
   - 针对服务层逻辑进行测试
   - 使用Mockito模拟依赖组件

2. **集成测试**
   - 测试数据库操作和业务流程
   - 使用真实的数据库环境

3. **控制器测试**
   - 验证API端点的正确性
   - 测试请求和响应处理

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
请检查application.properties中的数据库配置是否正确，确保MySQL服务正在运行。

### 2. API文档无法访问
确认应用已成功启动，访问 http://localhost:8080/doc.html 查看API文档。

### 3. AI文档生成功能不可用
检查是否正确配置了OpenAI API密钥，该功能需要有效的API密钥才能工作。