# DocumentationGenerator_Back

该项目是 Mintlify ProjectDocumentationGenerator 后端的 Java 重写版，基于 **Spring Boot 3 + MySQL + MyBatis** 实现，与原 Node.js 服务保持接口兼容性，支持 IDE 插件、公共 API 与后台工具的无缝迁移。

## 功能概览

- 📄 **AI 文档生成**：异步生成代码注释/解释，完全复刻 `/docs/*` 队列逻辑与反馈机制。
- 👥 **团队协作**：支持成员邀请、配额统计，与 Premium 付费约束保持一致。
- 🔑 **API Key 管理**：提供 Typeform Webhook、管理员手动发放以及公共 API 访问校验。
- 🧩 **第三方集成**：对接 Auth0 登录、Stripe 订阅事件，兼容原服务的用户升级流程。
- 📊 **调试与辅助**：保留 Playground、进度统计、欢迎页面等路由供插件与后台使用。

> 详细接口说明见 [`API_DOCUMENTATION.md`](API_DOCUMENTATION.md)。

## 技术栈

| 模块 | 说明 |
| --- | --- |
| 核心框架 | Spring Boot 3, Spring MVC, Spring Security (保留 JWT 能力) |
| 数据持久化 | MyBatis（注解 Mapper），MySQL 8.x |
| AI 集成 | Spring AI（OpenAI Chat 接口，可根据需要替换） |
| 队列执行 | 基于 Java 线程池的内存任务调度，模拟原 Redis + Bull 工作流 |
| 其他 | Lombok、Knife4j、JUnit5/Mockito（待补充测试） |

## 环境准备

1. **必需软件**
   - Java 17+
   - MySQL 8.0+
   - Maven 3.6+（或使用本地 `mvnw` 包装器）
2. **数据库初始化**
   ```sql
   CREATE DATABASE doc_generator CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
   执行 `src/main/resources/schema.sql` 以创建所需表结构。
3. **配置文件** 主要在 `src/main/resources/application.yml`：
   - `spring.datasource.*`：MySQL 连接信息
   - `spring.ai.openai.api-key`：可选，启用 AI 能力时填写
   - `jwt.secret`：仍保留 JWT 鉴权所需密钥
   - `docgen.ai.*`：选择 AI 供应商（OpenAI / DeepSeek）以及对应模型配置
4. **关键环境变量（按需启用）**

   | 变量名 | 用途 |
   | --- | --- |
   | `ADMIN_ACCESS_KEY` | `/functions/*` 与 `/playground/*` 管理入口校验 |
   | `AUTH0_ISSUER_BASE_URL` / `AUTH0_CLIENT_ID` / `AUTH0_CLIENT_SECRET` | `/user/code` Auth0 授权兑换 |
   | `STRIPE_SECRET_KEY`（可选） | Stripe SDK 使用时需要；当前控制器仅读取 webhook 数据 |
   | `OPENAI_API_KEY` | Spring AI 调用 OpenAI 时使用 |
   | `DEEPSEEK_API_KEY`（可选） | 切换至 DeepSeek 模型时的 API Key |

### 切换至 DeepSeek

`docgen.ai.provider` 默认为 `openai`。如需使用 DeepSeek：

1. 在 `application.yml` 中设置：
   ```yaml
   docgen:
     ai:
       provider: deepseek
       deepseek:
         model: deepseek-chat # 或 deepseek-coder
         api-key: ${DEEPSEEK_API_KEY}
   ```
2. 配置环境变量 `DEEPSEEK_API_KEY`（或直接在配置文件中填写密钥）。

其余路由与调用方式保持不变。

## 启动项目

```bash
# 编译并运行（需已安装 Maven）
mvn spring-boot:run

# 或使用可执行 Jar
mvn clean package
java -jar target/DocumentationGenerator-0.0.1-SNAPSHOT.jar
```

默认监听 `http://localhost:8080`。首次启动会在 `docs`、`users`、`teams` 等表中写入数据。

## 目录结构

```
src/
├── main/
│   ├── java/
│   │   └── com/codecraft/documentationgenerator/
│   │       ├── controller/       # 与原 Mintlify 路由对应的 REST 控制器
│   │       ├── entity/           # MyBatis 实体
│   │       ├── mapper/           # 注解 Mapper & TypeHandler
│   │       ├── service/          # 业务接口
│   │       │   └── impl/         # 任务队列、团队逻辑等实现
│   │       ├── service/jobs/     # 文档生成任务模型
│   │       └── util/             # 注释包装、语言辅助工具
│   └── resources/
│       ├── application.yml       # Spring 配置
│       ├── mapper/               # (如需 XML Mapper)
│       └── schema.sql            # 数据库初始化脚本
└── test/                         # 单元 / 集成测试 (待完善)
```

## 核心流程说明

1. **文档生成**：`DocsController` 接收请求 → `DocJobService` 入队 → AI/启发式生成 → 持久化至 `docs` 表 → `/docs/worker/{id}` 提供轮询结果。
2. **配额与鉴权**：`DocJobService` 根据 `userId` 统计 30 天内生成次数，超限时返回 `requiresAuth` 响应，与原插件行为一致。
3. **团队同步**：`TeamController` 结合 `TeamServiceImpl` 读取/写入 JSON 成员列表，限制 Premium 用户最多 2 个成员。
4. **公共 API**：`PublicApiController` 按原逻辑校验 API Key（SHA-1）并提供语言/格式列表。
5. **Webhook**：`WebhooksController` 更新用户订阅状态，保留 Stripe 事件兼容性。

## 日志与观测

- **入口日志**：`DocsController`、`TeamController`、`PublicApiController`、`PlaygroundController`、`FunctionsController`、`UserController` 等核心控制器会在 INFO 级别记录请求关键信息（如用户、语言、操作结果），并在 DEBUG 级别输出上下文尺寸、队列长度等细节。
- **异步任务**：`DocJobService` 会跟踪任务受理、排队、执行、完成/失败的完整生命周期，并在配额校验、AI 生成、反馈决策等步骤输出调试信息，便于排查长耗时或异常。
- **第三方回调**：Stripe Webhook 与 Typeform Webhook 会输出脱敏后的邮箱、客户 ID，未识别或缺失字段会以 WARN 级别提示。
- **日志级别配置**：可在 `application.yml` 中自定义输出级别，例如：

  ```yaml
  logging:
    level:
      com.codecraft.documentationgenerator: INFO   # 生产环境推荐
      com.codecraft.documentationgenerator.service.impl.DocJobService: DEBUG
  ```

  本地调试需要更详细信息时，可将对应包设置为 `DEBUG`；生产环境建议保持 INFO 并结合集中式日志采集（如 ELK、CloudWatch）。

## 如何迁移自 Node.js 版本

1. 将原 Mongo/Redis 数据迁移至 MySQL，字段映射详见 `schema.sql`。
2. 按需同步 `API-KEY`、团队成员、Stripe 客户等数据——实体结构与原模型一致。
3. 更新 IDE 插件 / 外部调用方的基础 URL 即可，无需调整请求结构。

## 常见问题

- **未安装 Maven**：请安装 `maven` 或引入 `mvnw` 包装器后再运行启动命令。
- **OpenAI 调用失败**：确认环境变量 `OPENAI_API_KEY` 是否正确，或替换为自定义模型实现。
- **Auth0/Stripe 可选**：如暂不需要第三方登录或计费，可不配置相关变量，接口会返回合理的兜底响应。
- **日志过多**：可在 `application.yml` 中调低特定包的日志级别，或通过 `logging.pattern.*` 自定义输出模板。

## 测试

- 运行单元/集成测试：`mvn -B test`
- 当前测试覆盖核心控制器与服务，包含 22 个 JUnit 5 用例；若新增功能，请同步补充测试并保证该命令通过。

## 相关文档

- [接口列表](API_DOCUMENTATION.md)
- [使用示例与调试指南](USAGE_GUIDE.md)
