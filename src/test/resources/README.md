# 测试指南

## 运行所有测试

使用 Maven 运行所有测试：

```bash
mvn test
```

## 运行特定测试类

```bash
mvn -Dtest=UserServiceImplTest test
mvn -Dtest=DocServiceImplTest test
mvn -Dtest=TeamServiceImplTest test
mvn -Dtest=ApiKeyServiceImplTest test
mvn -Dtest=GlobalExceptionHandlerTest test
```

## 运行测试套件

```bash
mvn -Dtest=AllTestsSuite test
```

## 测试类型说明

本项目包含两种类型的测试：

1. **单元测试** - 测试单个类或方法的功能，不依赖Spring上下文：
   - UserServiceImplTest
   - DocServiceImplTest
   - TeamServiceImplTest
   - ApiKeyServiceImplTest
   - BusinessExceptionTest
   - GlobalExceptionHandlerTest
   - ExceptionHandlingTest
   - MessageConstantsTest

2. **集成测试** - 测试多个组件协同工作，需要Spring上下文：
   - GlobalExceptionHandlerIntegrationTest

## 测试覆盖的功能

1. **UserService测试** - 覆盖用户相关的所有功能：
   - 根据ID查找用户
   - 根据邮箱查找用户
   - 创建新用户
   - 删除用户
   - 获取所有用户

2. **DocService测试** - 覆盖文档相关的所有功能：
   - 根据ID查找文档
   - 根据反馈ID查找文档
   - 创建新文档
   - 删除文档
   - 获取所有文档
   - 根据用户ID查找文档

3. **TeamService测试** - 覆盖团队相关的所有功能：
   - 根据ID查找团队
   - 根据管理员邮箱查找团队
   - 创建新团队
   - 删除团队
   - 获取所有团队

4. **ApiKeyService测试** - 覆盖API密钥相关的所有功能：
   - 根据ID查找API密钥
   - 根据哈希值查找API密钥
   - 创建新API密钥
   - 删除API密钥
   - 获取所有API密钥

5. **异常处理测试** - 覆盖全局异常处理机制：
   - BusinessException 类的基本功能
   - GlobalExceptionHandler 对业务异常的处理
   - GlobalExceptionHandler 对系统异常的处理
   - 集成测试验证异常处理在Web层的正确性
   - 服务层异常抛出的验证

6. **常量类测试** - 覆盖消息常量类的测试：
   - MessageConstants 类中所有常量的验证
   - 私有构造函数的测试

## 异常处理测试详情

新增的异常处理功能包含以下测试：

1. **BusinessExceptionTest** - 测试自定义业务异常类：
   - 带消息的构造函数
   - 带消息和原因的构造函数

2. **GlobalExceptionHandlerTest** - 测试全局异常处理器：
   - 业务异常处理方法
   - 通用异常处理方法
   - 返回正确的HTTP状态码和响应格式

3. **GlobalExceptionHandlerIntegrationTest** - 集成测试：
   - 验证在Web请求中异常处理的完整流程
   - 确保返回正确的JSON格式响应

4. **ExceptionHandlingTest** - 服务层异常处理测试：
   - 验证BusinessException继承关系
   - 验证异常消息和原因的正确传递

## 异常处理机制验证

所有测试都验证了以下异常处理机制：

1. 服务层在适当情况下抛出 BusinessException
2. 全局异常处理器正确捕获 BusinessException
3. BusinessException 返回 400 状态码和具体错误信息
4. 其他未捕获异常返回 500 状态码和通用错误信息
5. 所有异常响应都采用统一的 JSON 格式：{code: 状态码, message: 错误信息, data: null}

## 运行集成测试

要单独运行集成测试，可以使用：

```bash
mvn -Dtest=GlobalExceptionHandlerIntegrationTest test
```