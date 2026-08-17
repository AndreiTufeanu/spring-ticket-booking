package com.andreitufeanu.backend.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    @Around("execution(* com.andreitufeanu.backend..service.*Service.*(..))")
    public Object logInvocation(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        log.debug("-> {} args={}", method, Arrays.toString(joinPoint.getArgs()));

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();

        log.debug("<- {} ({} ms) result={}", method, System.currentTimeMillis() - start, result);
        return result;
    }
}