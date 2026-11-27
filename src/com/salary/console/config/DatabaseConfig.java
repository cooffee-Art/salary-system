package com.salary.console.config;

/**
 * 数据库连接配置。
 *
 * 密码不写死在代码里：优先读环境变量 SALARY_DB_PASSWORD，
 * 没有设置时使用占位值，开发者按 README 第 6 节在本机设置即可。
 *
 * Windows PowerShell 示例：
 *   $env:SALARY_DB_PASSWORD="你的MySQL密码"
 * macOS / Linux：
 *   export SALARY_DB_PASSWORD="你的MySQL密码"
 */
public final class DatabaseConfig {

    public static final String URL = System.getenv().getOrDefault(
            "SALARY_DB_URL",
            "jdbc:mysql://localhost:3306/salary_db"
                    + "?useUnicode=true&characterEncoding=UTF-8"
                    + "&serverTimezone=Asia/Shanghai"
                    + "&useSSL=false&allowPublicKeyRetrieval=true");

    public static final String USER = System.getenv().getOrDefault("SALARY_DB_USER", "root");

    public static final String PASSWORD = System.getenv().getOrDefault(
            "SALARY_DB_PASSWORD", "your_mysql_password");

    private DatabaseConfig() {
    }

    /**
     * 是否仍在用占位密码，用于启动时给出友好提示。
     */
    public static boolean usesPlaceholderPassword() {
        return "your_mysql_password".equals(PASSWORD);
    }
}
