package com.salary.console.service;

import com.salary.console.db.DatabaseManager;
import com.salary.console.i18n.I18n;
import com.salary.console.service.impl.InMemoryAuthService;
import com.salary.console.service.impl.JdbcAuthService;

/**
 * 登录服务工厂：优先使用真实数据库，数据库不可用时退回内存演示账号。
 *
 * 这样即使组员本机没装 MySQL，程序也能启动、能演示菜单与双语；
 * 一旦配好数据库，自动走 JDBC 登录，不需要改代码。
 */
public final class AuthServiceFactory {

    private AuthServiceFactory() {
    }

    public static AuthService create() {
        if (DatabaseManager.isAvailable()) {
            System.out.println(I18n.get("login.backendJdbc"));
            return new JdbcAuthService();
        }

        String reason = DatabaseManager.unavailableReason();
        System.out.println(I18n.get("login.backendMemory"));
        if (!reason.isBlank()) {
            System.out.println(I18n.get("login.backendMemoryReason", reason));
        }
        return new InMemoryAuthService();
    }
}
