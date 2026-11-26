package com.salary.console.service.impl;

import com.salary.console.model.Role;
import com.salary.console.model.Session;
import com.salary.console.service.AuthService;

import java.util.Map;
import java.util.Optional;

/**
 * 临时登录实现，仅用于控制台骨架跑通。
 *
 * TODO(Lucas): 替换为 JDBC 查询 sys_user，并校验密码哈希。
 */
public final class InMemoryAuthService implements AuthService {

    private final Map<String, Credential> users = Map.of(
            "admin", new Credential("Admin@123", Role.ADMIN),
            "employee", new Credential("User@123", Role.EMPLOYEE)
    );

    @Override
    public Optional<Session> login(String username, String password) {
        Credential credential = users.get(username);
        if (credential == null || !credential.password().equals(password)) {
            return Optional.empty();
        }
        return Optional.of(new Session(username, credential.role()));
    }

    private record Credential(String password, Role role) {
    }
}
