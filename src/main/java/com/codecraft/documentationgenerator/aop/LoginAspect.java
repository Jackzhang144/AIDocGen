package com.codecraft.documentationgenerator.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 登录检查切面类
 * <p>
 * 用于检查用户是否已经登录，如果未登录则返回未授权状态
 *
 * @author CodeCraft
 * @version 1.0
 */
@Slf4j
@Aspect
@Component
public class LoginAspect {

    /**
     * 环绕通知，检查用户登录状态
     * <p>
     * 当方法被@RequireLogin注解标记时，会执行此方法进行登录检查
     *
     * @param joinPoint    连接点
     * @param requireLogin RequireLogin注解
     * @return 方法执行结果或未授权响应
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(requireLogin)")
    public Object checkLogin(ProceedingJoinPoint joinPoint, RequireLogin requireLogin) throws Throwable {
        log.info("Checking user login status for method: {}", joinPoint.getSignature().getName());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("User not logged in, returning unauthorized response");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未登录");
        }

        log.info("User is logged in, proceeding with method execution");
        return joinPoint.proceed();
    }
}