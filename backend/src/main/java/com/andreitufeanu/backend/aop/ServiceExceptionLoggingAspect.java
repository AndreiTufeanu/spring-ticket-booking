package com.andreitufeanu.backend.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceExceptionLoggingAspect {

    @AfterThrowing(pointcut = "execution(* com.andreitufeanu.backend..service.*Service.*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        log.warn("{} threw {}: {}", joinPoint.getSignature().toShortString(), ex.getClass().getSimpleName(), ex.getMessage());
    }
}