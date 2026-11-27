package com.salary.console.dao;

import com.salary.console.model.Role;

/**
 * 从 sys_user 读出的账号信息，只用于登录校验。
 * passwordHash 只在 AuthService 内部使用，不进入 Session。
 */
public record UserRecord(
        int id,
        String username,
        String passwordHash,
        Role role,
        int status,
        Integer empId
) {

    public boolean isEnabled() {
        return status == 1;
    }
}
