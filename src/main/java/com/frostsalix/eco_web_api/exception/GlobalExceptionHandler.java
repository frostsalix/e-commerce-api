package com.frostsalix.eco_web_api.exception;

import com.frostsalix.eco_web_api.common.ApiResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理 @Valid 校验失败
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidException(MethodArgumentNotValidException e) {

        String msg = Objects.requireNonNull(e.getBindingResult()
                        .getFieldError())
                .getDefaultMessage();

        return new ApiResponse<>(400, msg, null);
    }

    // 处理运行时异常
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handleRuntimeException(RuntimeException e) {

        return new ApiResponse<>(500, e.getMessage(), null);
    }

    // 兜底异常
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {

        return new ApiResponse<>(500, "系统异常", null);
    }
}