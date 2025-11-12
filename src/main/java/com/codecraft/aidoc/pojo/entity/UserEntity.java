package com.codecraft.aidoc.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.codecraft.aidoc.enums.UserRole;
import lombok.Data;
import org.apache.ibatis.type.EnumTypeHandler;

import java.time.LocalDateTime;

/**
 * Represents an authenticated user that can access the AIDocGen backend and admin panel.
 */
@Data
@TableName("users")
public class UserEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    @TableField("password_hash")
    private String passwordHash;

    private String email;

    @TableField(value = "role", typeHandler = EnumTypeHandler.class)
    private UserRole role;

    @TableField("api_quota")
    private Integer apiQuota;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
