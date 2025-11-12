package com.codecraft.aidoc.pojo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Registration payload for new accounts.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @Email(message = "邮箱格式错误")
    private String email;

    @NotBlank(message = "密码不能为空")
    private String password;
}
