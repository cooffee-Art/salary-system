package com.example.salarysystem.security;

/**
 * 放进 SecurityContext 的当前登录人，不包含密码。
 */
public record AuthUser(String username, String role) {
}
