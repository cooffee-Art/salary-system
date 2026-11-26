package com.salary.console.ui;

import com.salary.console.i18n.I18n;

import java.util.List;

/**
 * 通用控制台菜单：渲染标题、编号选项和退出项。
 * 返回 null 表示用户选择 0（退出当前菜单）。
 */
public final class Menu {

    private final String titleKey;
    private final List<MenuItem> items;
    private final InputReader input;

    public Menu(String titleKey, List<MenuItem> items, InputReader input) {
        this.titleKey = titleKey;
        this.items = items;
        this.input = input;
    }

    public MenuItem select() {
        System.out.println();
        System.out.println("=".repeat(64));
        System.out.println(I18n.get("app.title"));
        System.out.println(I18n.get(titleKey));
        System.out.println("=".repeat(64));

        for (int index = 0; index < items.size(); index++) {
            System.out.printf(" %d. %s%n", index + 1, I18n.get(items.get(index).labelKey()));
        }
        System.out.printf(" 0. %s%n", I18n.get("common.exit"));

        int choice = input.readInt("common.select", 0, items.size());
        if (choice == 0) {
            return null;
        }
        return items.get(choice - 1);
    }
}
