package com.example.english_learning_platform.exception;

import com.example.english_learning_platform.util.Result;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // 全局捕获控制器异常
public class GlobalExceptionHandler {

    // 处理参数非法异常
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleIllegalArgument(IllegalArgumentException e) {
        return Result.error(e.getMessage());
    }

    // 处理资源不存在异常
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleEntityNotFound(EntityNotFoundException e) {
        return Result.error(e.getMessage());
    }

    // 处理权限异常
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handleSecurity(SecurityException e) {
        return Result.error(e.getMessage());
    }

    // 处理通用运行时异常
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleRuntime(RuntimeException e) {
        return Result.error("服务器内部错误：" + e.getMessage());
    }
}