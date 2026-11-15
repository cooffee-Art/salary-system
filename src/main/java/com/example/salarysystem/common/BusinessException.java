package com.example.salarysystem.common;

import lombok.Getter;

/** 可直接向用户展示的业务错误，由接口异常处理器统一转换。 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
