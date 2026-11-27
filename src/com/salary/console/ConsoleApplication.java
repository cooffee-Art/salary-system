package com.salary.console;

import com.salary.console.i18n.I18n;
import com.salary.console.model.Role;
import com.salary.console.model.Session;
import com.salary.console.service.AuthService;
import com.salary.console.service.Permission;
import com.salary.console.service.PermissionGuard;
import com.salary.console.ui.InputReader;
import com.salary.console.ui.Menu;
import com.salary.console.ui.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 控制台应用主流程：
 * 语言选择 → 登录 → 角色菜单 → 退出登录 / 退出系统。
 *
 * 其他成员只需要替换菜单动作或注入新的 Service，不需要改主循环。
 */
public final class ConsoleApplication {

    private final InputReader input;
    private final AuthService authService;
    private boolean running = true;

    public ConsoleApplication(InputReader input, AuthService authService) {
        this.input = input;
        this.authService = authService;
    }

    public void run() {
        if (!chooseLanguage()) {
            System.out.println(I18n.get("common.goodbye"));
            return;
        }

        while (running) {
            Session session = login();
            if (session == null) {
                break;
            }
            showMainMenu(session);
        }

        System.out.println(I18n.get("common.goodbye"));
    }

    private boolean chooseLanguage() {
        MenuItem selected = new Menu(
                "language.title",
                List.of(
                        new MenuItem("lang-en", "language.english", () -> {
                        }),
                        new MenuItem("lang-zh", "language.chinese", () -> {
                        })
                ),
                input
        ).select();

        if (selected == null) {
            return false;
        }

        if ("lang-en".equals(selected.id())) {
            I18n.setLocale(Locale.ENGLISH);
        } else {
            I18n.setLocale(Locale.SIMPLIFIED_CHINESE);
        }
        System.out.println(I18n.get("language.changed"));
        return true;
    }

    private Session login() {
        System.out.println();
        System.out.println(I18n.get("login.title"));
        System.out.println(I18n.get("login.exitHint"));

        while (running) {
            String username = input.readLine("login.username");
            if ("0".equals(username)) {
                running = false;
                return null;
            }
            String password = input.readLine("login.password");

            Optional<Session> session = authService.login(username, password);
            if (session.isPresent()) {
                Session loggedIn = session.get();
                System.out.println(I18n.get(
                        "login.success",
                        loggedIn.username(),
                        I18n.get(loggedIn.role().labelKey())
                ));
                return loggedIn;
            }
            System.out.println(I18n.get("login.failed"));
        }
        return null;
    }

    private void showMainMenu(Session session) {
        boolean loggedIn = true;

        while (running && loggedIn) {
            List<MenuItem> items = new ArrayList<>();

            if (session.role() == Role.ADMIN) {
                items.add(new MenuItem("employee", "menu.employee",
                        () -> guarded(session, Permission.MANAGE_EMPLOYEE, "module.employee")));
                items.add(new MenuItem("salary", "menu.salary",
                        () -> guarded(session, Permission.MANAGE_SALARY, "module.salary")));
                items.add(new MenuItem("report", "menu.report",
                        () -> guarded(session, Permission.VIEW_REPORT, "module.report")));
            } else {
                items.add(new MenuItem("profile", "menu.profile",
                        () -> guarded(session, Permission.VIEW_OWN_PROFILE, "module.profile")));
                items.add(new MenuItem("my-salary", "menu.mySalary",
                        () -> guarded(session, Permission.VIEW_OWN_SALARY, "module.mySalary")));
            }

            items.add(new MenuItem("language", "menu.switchLanguage", () -> {
            }));
            items.add(new MenuItem("logout", "menu.logout", () -> {
            }));

            MenuItem selected = new Menu("menu.main", items, input).select();
            if (selected == null) {
                running = false;
                return;
            }

            switch (selected.id()) {
                case "logout" -> {
                    loggedIn = false;
                    System.out.println(I18n.get("login.loggedOut"));
                }
                case "language" -> {
                    if (!chooseLanguage()) {
                        running = false;
                        return;
                    }
                }
                default -> selected.action().run();
            }
        }
    }

    /**
     * 统一入口：先过角色权限，再进入模块。
     * 模块由其他成员实现后，把这里的 notImplemented(...) 换成对应 Service 调用即可，
     * 权限判断不需要各模块各写一遍。
     */
    private void guarded(Session session, Permission permission, String moduleKey) {
        if (PermissionGuard.require(session, permission)) {
            notImplemented(moduleKey);
        }
    }

    private void notImplemented(String moduleKey) {
        System.out.println(I18n.get("common.notImplemented", I18n.get(moduleKey)));
    }
}
