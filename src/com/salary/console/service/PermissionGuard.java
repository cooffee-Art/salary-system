package com.salary.console.service;

import com.salary.console.i18n.I18n;
import com.salary.console.model.Role;
import com.salary.console.model.Session;

/**
 * 角色权限拦截。
 *
 * 菜单层按角色裁剪入口只是第一道；
 * 各模块在执行具体动作前仍应调用 require(...)，防止越权调用同一方法。
 *
 * 用法（Ming / Leo 的模块里）：
 *   if (!PermissionGuard.require(session, Permission.MANAGE_EMPLOYEE)) {
 *       return;
 *   }
 *   // 真正的业务逻辑
 */
public final class PermissionGuard {

    private PermissionGuard() {
    }

    /**
     * 拦截并给出双语提示；无权时返回 false，调用方直接 return。
     */
    public static boolean require(Session session, Permission permission) {
        if (isAllowed(session, permission)) {
            return true;
        }
        System.out.println(I18n.get(
                "permission.denied",
                I18n.get(permission.labelKey()),
                I18n.get(session == null ? "role.anonymous" : session.role().labelKey())
        ));
        return false;
    }

    /**
     * 纯判断，便于白盒测试直接验证角色矩阵。
     */
    public static boolean isAllowed(Session session, Permission permission) {
        if (session == null) {
            return false;
        }
        return isAllowed(session.role(), permission);
    }

    public static boolean isAllowed(Role role, Permission permission) {
        if (role == null) {
            return false;
        }
        return switch (permission) {
            case MANAGE_EMPLOYEE, MANAGE_SALARY, VIEW_REPORT -> role == Role.ADMIN;
            case VIEW_OWN_PROFILE -> true;
            case VIEW_OWN_SALARY -> true;
        };
    }
}
