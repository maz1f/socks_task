package org.example.sockstask.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethods() {}

    @Pointcut("within(org.example.sockstask.exception.handler.GlobalExceptionHandler)")
    public void exceptionHandlerMethods() {}

    @Before("serviceMethods()")
    public void logBeforeServiceMethods(JoinPoint joinPoint) {
        log.info("Call method {} with args: {}",
                joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "exceptionHandlerMethods()", returning = "result")
    public void logBeforeExceptionHandlerMethods(JoinPoint joinPoint, ResponseEntity<String> result) {
        log.error("Catch exception {} with message: {}",
                joinPoint.getArgs()[0].getClass().getSimpleName(), result.getBody());
    }

}
