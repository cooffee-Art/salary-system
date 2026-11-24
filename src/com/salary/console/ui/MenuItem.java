package com.salary.console.ui;

/**
 * 一个菜单项。
 *
 * @param id       稳定的程序标识，不参与翻译，用于 switch 判断
 * @param labelKey 语言包 key
 * @param action   选中后执行的动作
 */
public record MenuItem(String id, String labelKey, Runnable action) {
}
