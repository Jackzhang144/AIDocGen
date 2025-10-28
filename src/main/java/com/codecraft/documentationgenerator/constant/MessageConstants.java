package com.codecraft.documentationgenerator.constant;

/**
 * 消息常量类
 * <p>
 * 存储项目中使用的所有消息常量，便于统一管理和维护
 *
 * @author CodeCraft
 * @version 1.0
 */
public class MessageConstants {
    
    // 用户相关消息常量
    public static final String USER_NOT_FOUND = "用户不存在";
    public static final String USER_ALREADY_EXISTS = "用户已存在";
    public static final String EMAIL_CANNOT_BE_EMPTY = "邮箱不能为空";
    
    // 文档相关消息常量
    public static final String DOCUMENT_NOT_FOUND = "文档不存在";
    public static final String CODE_REQUIRED = "代码不能为空";
    
    // API密钥相关消息常量
    public static final String API_KEY_NOT_FOUND = "API密钥不存在";
    
    // 团队相关消息常量
    public static final String TEAM_NOT_FOUND = "团队不存在";
    
    // 认证相关消息常量
    public static final String USER_NOT_LOGGED_IN = "用户未登录";
    public static final String REGISTRATION_SUCCESS = "注册成功";
    public static final String INVALID_CREDENTIALS = "用户名或密码错误";
    public static final String LOGIN_SUCCESS = "登录成功";
    
    // 服务器相关消息常量
    public static final String SERVER_ERROR = "服务器异常";
    
    // 私有构造函数，防止实例化
    private MessageConstants() {
        throw new AssertionError("Cannot instantiate constant class");
    }
}
