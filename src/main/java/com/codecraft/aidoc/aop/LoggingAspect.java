package com.codecraft.aidoc.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Cross-cutting logging concern that traces controller and service invocations alongside timing metrics.
 */
@Slf4j
@Aspect
@Component
@Order(1)
public class LoggingAspect {

    /**
     * Logs method invocation boundaries for all classes within the controller and service package hierarchy.
     *
     * @param joinPoint current execution context
     * @return result produced by the intercepted method
     * @throws Throwable propagated underlying exceptions
     */
    @Around("execution(* com.codecraft.aidoc.controller..*(..)) || execution(* com.codecraft.aidoc.service..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        final String signature = joinPoint.getSignature().toShortString();
        final long start = System.currentTimeMillis();
        log.debug("[Aidoc] 进入 {}", signature);
        try {
            Object result = joinPoint.proceed();
            final long elapsed = System.currentTimeMillis() - start;
            log.debug("[Aidoc] 完成 {} 耗时 {} ms", signature, elapsed);
            return result;
        } catch (Throwable throwable) {
            final long elapsed = System.currentTimeMillis() - start;
            log.warn("[Aidoc] 执行 {} 发生异常，耗时 {} ms，原因：{}", signature, elapsed, throwable.getMessage());
            throw throwable;
        }
    }
}
