package com.salary.console.service;

import com.salary.console.model.Session;

import java.util.Optional;

/**
 * 登录服务接口。
 * 当前由 InMemoryAuthService 提供演示实现，Lucas 后续替换为 JDBC 版本。
 */
public interface AuthService {

    Optional<Session> login(String username, String password);
}
