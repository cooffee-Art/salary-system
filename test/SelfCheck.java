import com.salary.console.dao.UserDao;
import com.salary.console.db.DatabaseManager;
import com.salary.console.i18n.I18n;
import com.salary.console.model.Role;
import com.salary.console.model.Session;
import com.salary.console.security.PasswordHasher;
import com.salary.console.service.Permission;
import com.salary.console.service.PermissionGuard;
import com.salary.console.service.impl.JdbcAuthService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 白盒自检：不经过控制台界面，直接验证登录、权限与哈希的边界条件。
 *
 * 运行（在项目根目录，已 javac 编译 src 到 out/）：
 *   javac -encoding UTF-8 -cp "out" -d test-out test/SelfCheck.java
 *   java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "out;test-out;resources;lib/*" SelfCheck
 *
 * 退出码 0 表示全部通过，1 表示存在失败项。
 */
public final class SelfCheck {

    private static final AtomicInteger PASSED = new AtomicInteger();
    private static final AtomicInteger FAILED = new AtomicInteger();

    private SelfCheck() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("==== 控制台版白盒自检 ====");

        checkPasswordHasher();
        checkPermissionMatrix();
        checkI18nKeys();
        checkAuthAgainstDatabase();
        checkSqlInjectionSafe();
        checkUniqueUsername();

        System.out.println();
        System.out.printf("结果：通过 %d 项，失败 %d 项%n", PASSED.get(), FAILED.get());
        if (FAILED.get() > 0) {
            System.exit(1);
        }
    }

    private static void checkPasswordHasher() {
        System.out.println("[1] 密码哈希 PBKDF2");
        String hash = PasswordHasher.hash("Admin@123");
        assertTrue("哈希格式为 pbkdf2$sha256$...", hash.startsWith("pbkdf2$sha256$"));
        assertTrue("正确密码校验通过", PasswordHasher.verify("Admin@123", hash));
        assertTrue("错误密码校验失败", !PasswordHasher.verify("admin@123", hash));
        assertTrue("空密码校验失败", !PasswordHasher.verify("", hash));
        assertTrue("哈希串被篡改后校验失败",
                !PasswordHasher.verify("Admin@123", hash.replace("$sha256$", "$sha1$")));
        assertTrue("两次哈希结果不同（随机盐）", !hash.equals(PasswordHasher.hash("Admin@123")));

        String seededAdminHash = "pbkdf2$sha256$120000$QurRoGavwlRU2JADgZR8XQ==$vF1IAElSUbvpcdOgnNuXVG2lfXKURd6uurNYqB2nsmU=";
        String seededEmployeeHash = "pbkdf2$sha256$120000$cs/FJ6CMqjkb3D8LhUAwDA==$5X46ZtFB46qwJXWhLms4lmh3U0HmV94LRYOoj2UwZPA=";
        assertTrue("init.sql 中 admin 哈希可用", PasswordHasher.verify("Admin@123", seededAdminHash));
        assertTrue("init.sql 中 employee 哈希可用", PasswordHasher.verify("User@123", seededEmployeeHash));
    }

    private static void checkPermissionMatrix() {
        System.out.println("[2] 角色权限矩阵");
        Session admin = new Session("admin", Role.ADMIN);
        Session employee = new Session("employee", Role.EMPLOYEE);

        assertTrue("管理员可做员工管理", PermissionGuard.isAllowed(admin, Permission.MANAGE_EMPLOYEE));
        assertTrue("管理员可做薪资管理", PermissionGuard.isAllowed(admin, Permission.MANAGE_SALARY));
        assertTrue("管理员可看部门报表", PermissionGuard.isAllowed(admin, Permission.VIEW_REPORT));
        assertTrue("普通员工不能做员工管理", !PermissionGuard.isAllowed(employee, Permission.MANAGE_EMPLOYEE));
        assertTrue("普通员工不能做薪资管理", !PermissionGuard.isAllowed(employee, Permission.MANAGE_SALARY));
        assertTrue("普通员工不能看部门报表", !PermissionGuard.isAllowed(employee, Permission.VIEW_REPORT));
        assertTrue("普通员工可看本人信息", PermissionGuard.isAllowed(employee, Permission.VIEW_OWN_PROFILE));
        assertTrue("普通员工可看本人薪资", PermissionGuard.isAllowed(employee, Permission.VIEW_OWN_SALARY));
        assertTrue("未登录一律拒绝", !PermissionGuard.isAllowed((Session) null, Permission.VIEW_OWN_PROFILE));
    }

    private static void checkI18nKeys() {
        System.out.println("[3] 双语语言包完整性");
        String[] keys = {
                "login.backendJdbc", "login.backendMemory", "login.backendMemoryReason",
                "permission.denied", "permission.manageEmployee", "permission.manageSalary",
                "permission.viewReport", "role.anonymous",
                "login.failed", "login.success", "menu.employee", "menu.mySalary",
                "validation.email", "validation.phone", "validation.password"
        };

        I18n.setLocale(Locale.SIMPLIFIED_CHINESE);
        for (String key : keys) {
            String value = I18n.get(key);
            assertTrue("zh 有 key: " + key + " 且非占位", !value.startsWith("!"));
        }
        assertTrue("zh 权限提示使用中文", I18n.get("permission.manageEmployee").contains("员工"));

        I18n.setLocale(Locale.ENGLISH);
        for (String key : keys) {
            String value = I18n.get(key);
            assertTrue("en 有 key: " + key + " 且非占位", !value.startsWith("!"));
        }
        assertTrue("en 权限提示使用英文", I18n.get("permission.manageEmployee").contains("Employee"));

        // 恢复默认语言，避免影响同一 JVM 中的后续断言
        I18n.setLocale(Locale.SIMPLIFIED_CHINESE);
    }

    private static void checkAuthAgainstDatabase() throws Exception {
        System.out.println("[4] JDBC 登录（sys_user）");
        boolean available = DatabaseManager.isAvailable();
        assertTrue("数据库可连接", available);
        if (!available) {
            System.out.println("     原因：" + DatabaseManager.unavailableReason());
            return;
        }

        JdbcAuthService auth = new JdbcAuthService();

        Optional<Session> admin = auth.login("admin", "Admin@123");
        assertTrue("admin/Admin@123 登录成功", admin.isPresent());
        assertTrue("admin 角色为 ADMIN", admin.map(s -> s.role() == Role.ADMIN).orElse(false));

        Optional<Session> employee = auth.login("employee", "User@123");
        assertTrue("employee/User@123 登录成功", employee.isPresent());
        assertTrue("employee 角色为 EMPLOYEE", employee.map(s -> s.role() == Role.EMPLOYEE).orElse(false));

        assertTrue("错误密码登录失败", auth.login("admin", "admin123").isEmpty());
        assertTrue("密码大小写敏感", auth.login("admin", "admin@123").isEmpty());
        assertTrue("不存在的用户登录失败", auth.login("nobody", "whatever").isEmpty());
        assertTrue("空用户名登录失败", auth.login("", "Admin@123").isEmpty());
        assertTrue("空密码登录失败", auth.login("admin", "").isEmpty());

        // 停用账号不应能登录：临时插入一个 status=0 的账号后清理
        try (Connection connection = DatabaseManager.getConnection()) {
            insertDisabledUser(connection);
            try {
                assertTrue("status=0 的账号不能登录",
                        auth.login("disabled_user", "Admin@123").isEmpty());
            } finally {
                deleteUser(connection, "disabled_user");
            }
        }
    }

    private static void checkSqlInjectionSafe() throws Exception {
        System.out.println("[5] SQL 注入防护");
        if (!DatabaseManager.isAvailable()) {
            System.out.println("     跳过：数据库不可用");
            return;
        }
        JdbcAuthService auth = new JdbcAuthService();
        assertTrue("用户名 ' OR 1=1 -- 不能登录",
                auth.login("' OR 1=1 --", "anything").isEmpty());
        assertTrue("用户名 admin' -- 不能登录",
                auth.login("admin' --", "anything").isEmpty());
    }

    private static void checkUniqueUsername() throws Exception {
        System.out.println("[6] 用户名唯一键 uk_username");
        if (!DatabaseManager.isAvailable()) {
            System.out.println("     跳过：数据库不可用");
            return;
        }

        UserDao dao = new UserDao();
        assertTrue("admin 在库中存在", dao.countByUsername("admin") == 1);

        boolean rejected;
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO sys_user (username, password_hash, role, status) VALUES (?, ?, 'EMPLOYEE', 1)")) {
                statement.setString(1, "admin");
                statement.setString(2, PasswordHasher.hash("x"));
                statement.executeUpdate();
                rejected = false;
            } catch (SQLException exception) {
                rejected = true;
            }
        }
        assertTrue("重复用户名被唯一键拒绝", rejected);
        assertTrue("拒绝后 admin 仍只有 1 条", dao.countByUsername("admin") == 1);
    }

    private static void insertDisabledUser(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO sys_user (username, password_hash, role, status) VALUES (?, ?, 'EMPLOYEE', 0)")) {
            statement.setString(1, "disabled_user");
            statement.setString(2, PasswordHasher.hash("Admin@123"));
            statement.executeUpdate();
        }
    }

    private static void deleteUser(Connection connection, String username) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM sys_user WHERE username = ?")) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
    }

    private static void assertTrue(String name, boolean condition) {
        if (condition) {
            PASSED.incrementAndGet();
            System.out.println("  PASS  " + name);
        } else {
            FAILED.incrementAndGet();
            System.out.println("  FAIL  " + name);
        }
    }
}
