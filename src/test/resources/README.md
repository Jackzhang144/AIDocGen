# 测试指南

## 运行所有测试

使用 Maven 运行所有测试：

```bash
mvn test
```

若未安装 Maven，可在本地安装后执行上述命令。

## 测试结构总览

当前测试覆盖以下模块：

| 类别 | 具体用例 | 说明 |
| --- | --- | --- |
| 服务层 | `DocJobServiceTest` | 通过可注入的线程池验证文档队列的异步执行、配额限制及团队豁免，并校验 AI 生成结果的持久化逻辑 |
| 控制器层 | `DocsControllerTest`、`TeamControllerTest`、`PublicApiControllerTest`、`PlaygroundControllerTest` | 使用 MockMvc 验证核心路由的请求/响应形态及异常处理 |
| 工具层 | `CommentFormatterTest` | 覆盖注释换行与语言风格推断，确保生成的文档格式正确 |

> 旧版基于 Service/Exception 的单元测试已在重构中删除，新测试围绕 Java 重写后的业务流程构建。

## 运行单个测试类

```bash
mvn -Dtest=DocJobServiceTest test
mvn -Dtest=DocsControllerTest test
mvn -Dtest=TeamControllerTest test
mvn -Dtest=PublicApiControllerTest test
mvn -Dtest=PlaygroundControllerTest test
mvn -Dtest=CommentFormatterTest test
```

## 测试现状说明

- 所有测试均使用 Mockito 和 MockMvc，无需真实数据库或外部服务。
- `DocJobServiceTest` 使用单线程执行器，可稳定验证异步逻辑；如扩展实际队列实现，可在此基础上新增集成测试。
- 公共 API、团队协作等控制器测试同时验证 `GlobalExceptionHandler` 输出的错误格式，确保与生产行为一致。

后续若增加新接口或功能，请在各自模块下新增对应的测试类，并在此文档补充说明。***
