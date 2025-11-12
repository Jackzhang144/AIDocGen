package com.codecraft.aidoc.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents an API key record stored in the relational database.
 */
@Data
@TableName("api_keys")
public class ApiKeyEntity {

    /**
     * Surrogate identifier.
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * SHA-1 hashed API key. The raw value is only shown once upon creation.
     */
    @TableField("hashed_key")
    private String hashedKey;

    /**
     * Primary contact email.
     */
    private String email;

    /**
     * Last name of the contact.
     */
    @TableField("last_name")
    private String lastName;

    /**
     * First name of the contact.
     */
    @TableField("first_name")
    private String firstName;

    /**
     * Optional description detailing how the key will be used.
     */
    private String purpose;

    /**
     * Creation timestamp captured by the database.
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
