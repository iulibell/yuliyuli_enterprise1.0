package com.yuliyuli.aop;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yuliyuli.annotation.RateLimit;
import com.yuliyuli.util.RateLimiterUtil;

// AOP切面
@Aspect
@Component
public class RateLimitAOP {

    @Autowired
    private RateLimiterUtil rateLimiterUtil;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = rateLimit.key();
        if (key.isEmpty()) {
            // 默认使用方法名作为限流键
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            key = signature.getDeclaringTypeName() + "." + signature.getName();
        }
        
        if (!rateLimiterUtil.rateLimit(key, rateLimit.limit(), rateLimit.window())) {
            throw new RuntimeException("请求过于频繁，请稍后再试");
        }
        
        return joinPoint.proceed();
    }
}
