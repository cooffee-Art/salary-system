package com.salary.console.db;

import com.salary.console.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC 连接管理。
 *
 * 只负责「拿到连接」和「探测数据库是否可用」，
 * 具体 SQL 放到各 DAO 里，避免业务代码散落连接细节。
 */
public final class DatabaseManager {

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private DatabaseManager() {
    }

    /**
     * 取得一条新连接。调用方必须用 try-with-resources 关闭。
     */
    public static Connection getConnection() throws SQLException {
        loadDriver();
        return DriverManager.getConnection(
                DatabaseConfig.URL, DatabaseConfig.USER, DatabaseConfig.PASSWORD);
    }

    /**
     * 探测数据库是否可以连接。
     * 启动时用它决定用真实 JDBC 登录还是退回内存演示账号，
     * 避免没装 MySQL 时程序直接启动失败。
     */
    public static boolean isAvailable() {
        try (Connection ignored = getConnection()) {
            return true;
        } catch (SQLException | RuntimeException exception) {
            return false;
        }
    }

    /**
     * 探测失败时返回可读原因，方便写进启动提示和测试报告。
     */
    public static String unavailableReason() {
        try (Connection ignored = getConnection()) {
            return "";
        } catch (SQLException exception) {
            return exception.getMessage();
        } catch (RuntimeException exception) {
            return exception.getClass().getSimpleName() + ": " + exception.getMessage();
        }
    }

    private static void loadDriver() {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(
                    "MySQL Connector/J not found on classpath. "
                            + "Put lib/mysql-connector-j-8.4.0.jar under lib/ and add lib/* to -cp.",
                    exception);
        }
    }
}
