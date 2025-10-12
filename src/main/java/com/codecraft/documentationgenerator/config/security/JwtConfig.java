package com.codecraft.documentationgenerator.config.security;

import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT配置类
 * <p>
 * 提供JWT令牌加密解密所需的密钥配置
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Component
public class JwtConfig {

    /**
     * JWT签名密钥
     * <p>
     * 用于签名和验证JWT令牌的密钥
     */
    @Value("${jwt.secret:mySecretKeyForDocumentationGeneratorWhichIsVerySecureAndLongEnough}")
    private String SECRET_KEY;

    /**
     * 创建并返回用于JWT签名的密钥
     * <p>
     * 将字符串密钥转换为HMAC-SHA密钥
     *
     * @return SecretKey 用于JWT操作的密钥
     */
    @Bean
    public SecretKey secretKey() {
        log.info("Initializing JWT secret key");
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}