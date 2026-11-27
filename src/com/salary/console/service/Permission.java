package com.salary.console.service;

/**
 * 控制台模块权限点。
 *
 * 管理员：员工管理、薪资管理、部门报表、本人信息；
 * 普通员工：只能查看自己的信息与自己的薪资，不能进入管理类模块。
 */
public enum Permission {

    MANAGE_EMPLOYEE("permission.manageEmployee"),
    MANAGE_SALARY("permission.manageSalary"),
    VIEW_REPORT("permission.viewReport"),
    VIEW_OWN_PROFILE("permission.viewOwnProfile"),
    VIEW_OWN_SALARY("permission.viewOwnSalary");

    private final String labelKey;

    Permission(String labelKey) {
        this.labelKey = labelKey;
    }

    public String labelKey() {
        return labelKey;
    }
}
