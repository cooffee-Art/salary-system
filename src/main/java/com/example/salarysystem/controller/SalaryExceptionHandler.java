package com.example.salarysystem.controller;

import com.example.salarysystem.common.BusinessException;
import com.example.salarysystem.common.Result;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** 薪资模块范围异常处理，后续可由组长合并到全局异常处理器。业务码沿用 HTTP 200 契约。 */
@RestControllerAdvice(assignableTypes = {SalaryController.class})
public class SalaryExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> business(BusinessException exception) {
        return Result.error(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage()).sorted().findFirst().orElse("参数错误");
        return Result.error(400, message);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, HandlerMethodValidationException.class})
    public Result<Void> malformed(Exception exception) {
        return Result.error(400, "参数格式或取值不正确，请检查ID、金额和月份（yyyy-MM）");
    }

    // 唯一约束兜底：并发请求同时通过“同员工同月”预检查时，由数据库唯一键拦截。
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> duplicate(DuplicateKeyException exception) {
        return Result.error(400, "该员工该月已存在薪资记录");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> integrity(DataIntegrityViolationException exception) {
        return Result.error(400, "数据约束冲突，请确认员工和关联记录后重试");
    }
}
