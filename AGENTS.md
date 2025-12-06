# Repository Guidelines

## 项目结构与模块划分
- `backend/`：Spring Boot (Java 17)。业务代码在 `backend/src/main/java/com/codecraft`（分层：`controller`、`service`、`entity`、`repository`），配置与脚本在 `backend/src/main/resources`（`application.yml`、`schema.sql`），测试放置于 `backend/src/test/java` 对应包结构。
- `frontend/`：Vite + React。界面逻辑在 `frontend/src`（`components/`、`App.jsx`、`App.css`），静态资源在 `public/`，打包产物输出到 `dist/`。

## 构建、测试与本地运行
- Backend：`cd backend && mvn spring-boot:run` 启动本地服务（默认 8080），`mvn clean package` 构建 JAR，`mvn test` 运行 JUnit/Spring Boot 测试，`mvn clean` 清理产物。
- Frontend：`cd frontend && npm install`（首次），`npm run dev` 启动 Vite 开发服务器（5173，已允许本地 CORS），`npm run build` 生成生产包，`npm run lint` 进行 ESLint 检查。

## 代码风格与命名
- Java：4 空格缩进；类名以职责结尾（`*Controller`、`*Service`、`*Repository`、`*Tests`）；包名小写分层，如 `com.codecraft.controller`。使用 Lombok 减少样板，避免暴露可变公有字段。JSON 字段用小驼峰，与前端保持一致。
- React：函数式组件，文件/组件名用帕斯卡命名；自定义钩子以 `use` 开头。状态尽量局部，跨组件共享时再上提。样式避免全局污染，可在组件内局部样式或集中于 `App.css`。显式声明 props 类型（JS/TS 对应处理）。
- 格式化：前端用 `npm run lint`；后端遵循 IDE/Formatter 统一导入与 100–120 列宽。

## 测试要求
- 后端：JUnit 5 + Spring Boot Starter Test，测试路径镜像源码结构（例：`.../controller/AiControllerTests.java`）。对服务层逻辑、WebClient 调用、请求/响应契约与异常路径提供覆盖，可用 Reactor Test 断言异步流。
- 前端：当前未集成测试框架，新增前端功能建议引入 Vitest + React Testing Library；在此之前保持函数纯度与组件拆分，便于后续补测。

## 提交与 PR 规范
- 提交沿用 Conventional Commits 示例：`feat(app): ...`、`chore(config): ...`，使用一般现在时，作用域尽量精确。
- PR 需包含：变更摘要、关联 issue/需求、UI 变更前后说明（有界面则附截图），以及验证记录（`mvn test`、`npm run lint` 或手动步骤）。保持改动聚焦，变更行为需同步更新相关配置与文档。

## 安全与配置提示
- 切勿将密钥/数据库凭据提交到仓库。`backend/src/main/resources/application.yml` 中的敏感值应通过环境变量或外部配置覆盖。部署到非本地环境时，请收紧 CORS 允许域并确认数据库权限最小化。

## AI 功能约定
- 语言切换：前端支持中/英切换，影响 UI 文案与 AI 输出语言（请求携带 language）。
- 解释/注释仅作用于编辑器选中片段；未选中则提示。解释输出 100–150 字 Markdown（随语言），注释直接替换选区。
- 文档生成针对当前文件，生成 150–250 字 Markdown 摘要（随语言），展示在右侧 AI 面板，不落盘。
- 右侧 AI 面板常驻显示最新结果，标题随功能自动切换为“AI代码解释”或“AI文档”，并随语言更新。***

## 账户与权限
- 角色：USER（有限制）、MEMBER（无限制）、ADMIN（无限制）。普通用户每日调用上限由 `ai.daily-limit` 控制（默认 100）。
- 鉴权：注册/登录后获取 JWT，所有 AI 调用和管理接口需携带 `Authorization: Bearer <token>`。
- 管理员：启动时按 `admin.username/password` 自动创建。管理员可管理除自身以外的用户（创建、编辑角色/密码、删除）并查看 API 调用总量。***
