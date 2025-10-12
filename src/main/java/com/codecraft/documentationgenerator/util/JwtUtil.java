package com.codecraft.documentationgenerator.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 * <p>
 * 提供JWT令牌的生成、解析和验证功能
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Component
public class JwtUtil {

    @Autowired
    private SecretKey secretKey;

    /**
     * 从JWT令牌中提取邮箱
     *
     * @param token JWT令牌
     * @return String 用户邮箱
     */
    public String extractEmail(String token) {
        log.debug("Extracting email from JWT token");
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从JWT令牌中提取过期时间
     *
     * @param token JWT令牌
     * @return Date 过期时间
     */
    public Date extractExpiration(String token) {
        log.debug("Extracting expiration from JWT token");
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从JWT令牌中提取指定声明
     *
     * @param token          JWT令牌
     * @param claimsResolver 声明解析器
     * @param <T>            声明类型
     * @return T 声明值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析JWT令牌中的所有声明
     *
     * @param token JWT令牌
     * @return Claims 所有声明
     */
    private Claims extractAllClaims(String token) {
        log.debug("Extracting all claims from JWT token");
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    /**
     * 检查JWT令牌是否已过期
     *
     * @param token JWT令牌
     * @return Boolean 是否过期
     */
    private Boolean isTokenExpired(String token) {
        log.debug("Checking if JWT token is expired");
        return extractExpiration(token).before(new Date());
    }

    /**
     * 生成JWT令牌
     *
     * @param email 用户邮箱
     * @return String JWT令牌
     */
    public String generateToken(String email) {
        log.info("Generating JWT token for email: {}", email);
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email);
    }

    /**
     * 创建JWT令牌
     *
     * @param claims  声明
     * @param subject 主题（用户邮箱）
     * @return String JWT令牌
     */
    private String createToken(Map<String, Object> claims, String subject) {
        log.debug("Creating JWT token for subject: {}", subject);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10小时
                .signWith(secretKey)
                .compact();
    }

    /**
     * 验证JWT令牌
     *
     * @param token JWT令牌
     * @param email 用户邮箱
     * @return Boolean 验证结果
     */
    public Boolean validateToken(String token, String email) {
        log.info("Validating JWT token for email: {}", email);
        final String extractedEmail = extractEmail(token);
        boolean isValid = (extractedEmail.equals(email) && !isTokenExpired(token));
        log.debug("JWT token validation result: {}", isValid);
        return isValid;
    }
}