package com.salary.console.service.impl;

import com.salary.console.dao.UserDao;
import com.salary.console.dao.UserRecord;
import com.salary.console.model.Session;
import com.salary.console.security.PasswordHasher;
import com.salary.console.service.AuthService;

import java.sql.SQLException;
import java.util.Optional;

/**
 * 基于 MySQL 的登录实现，替换骨架期的 InMemoryAuthService。
 *
 * 校验顺序：
 * 1. 用户名查 sys_user（PreparedStatement，防注入）；
 * 2. 账号状态 status=1 才允许登录；
 * 3. PBKDF2 哈希比对密码，不比较明文；
 * 4. 全部通过才返回 Session。
 *
 * 用户名不存在与密码错误对外使用同一句提示，避免被用来探测账号是否存在。
 */
public final class JdbcAuthService implements AuthService {

    private final UserDao userDao;

    public JdbcAuthService() {
        this(new UserDao());
    }

    public JdbcAuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Optional<Session> login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return Optional.empty();
        }

        Optional<UserRecord> found;
        try {
            found = userDao.findByUsername(username.trim());
        } catch (SQLException exception) {
            throw new IllegalStateException("query sys_user failed: " + exception.getMessage(), exception);
        }

        if (found.isEmpty()) {
            return Optional.empty();
        }

        UserRecord user = found.get();
        if (!user.isEnabled()) {
            return Optional.empty();
        }
        if (!PasswordHasher.verify(password, user.passwordHash())) {
            return Optional.empty();
        }

        return Optional.of(new Session(user.username(), user.role()));
    }
}
