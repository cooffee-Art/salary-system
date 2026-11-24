package com.salary.console.model;

/**
 * 登录后的会话信息。只保存展示与权限判断需要的字段，不保存密码。
 */
public record Session(String username, Role role) {
}
