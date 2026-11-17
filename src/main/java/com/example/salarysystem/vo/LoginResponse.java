package com.example.salarysystem.vo;

import java.io.Serializable;

/**
 * 登录成功返回。前端把 token 放到 Authorization: Bearer xxx。
 */
public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        String username,
        String role
) implements Serializable {
}
