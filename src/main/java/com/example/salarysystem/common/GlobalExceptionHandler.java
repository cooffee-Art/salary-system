package com.example.salarysystem.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器：员工、部门、登录、薪资模块共用同一套返回结构。
 *
 * 业务约定：HTTP 状态码尽量保持 200，具体结果看 body.code。
 * 只有 Security 过滤链的 401/403 由 SecurityConfig 自己写 JSON。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> business(BusinessException exception) {
        return Result.error(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .sorted()
                .findFirst()
                .orElse("参数错误");
        return Result.error(400, message);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class,
            ConstraintViolationException.class})
    public Result<Void> malformed(Exception exception) {
        return Result.error(400, "参数格式或取值不正确，请检查ID、数字和日期（yyyy-MM-dd）");
    }

    /** 数据库唯一约束兜底，处理并发请求同时通过工号预检查的情况。 */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> duplicate(DuplicateKeyException exception) {
        log.warn("唯一约束冲突: {}", exception.getMessage());
        return Result.error(400, "数据已存在，请检查工号或月份是否重复");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> integrity(DataIntegrityViolationException exception) {
        log.warn("数据约束冲突: {}", exception.getMessage());
        return Result.error(400, "数据约束冲突，请确认部门和关联记录后重试");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> accessDenied(AccessDeniedException exception) {
        return Result.error(403, "没有权限执行该操作");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> notFound(NoResourceFoundException exception) {
        return Result.error(404, "接口不存在，请检查请求路径");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> methodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        return Result.error(405, "请求方法不支持");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> unknown(Exception exception) {
        log.error("未处理异常", exception);
        return Result.error(500, "服务器开小差了，请稍后再试");
    }
}
