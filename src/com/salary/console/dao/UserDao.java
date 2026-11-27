package com.salary.console.dao;

import com.salary.console.db.DatabaseManager;
import com.salary.console.model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

/**
 * sys_user 表的 JDBC 访问。
 *
 * 全部使用 PreparedStatement，用户名作为参数传入，
 * 不做字符串拼接，避免 SQL 注入。
 */
public final class UserDao {

    private static final String SELECT_BY_USERNAME = """
            SELECT id, username, password_hash, role, status, emp_id
            FROM sys_user
            WHERE username = ?
            """;

    /**
     * @return 命中账号返回记录；用户名不存在返回 empty
     */
    public Optional<UserRecord> findByUsername(String username) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USERNAME)) {

            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(resultSet));
            }
        }
    }

    private UserRecord mapRow(ResultSet resultSet) throws SQLException {
        int empId = resultSet.getInt("emp_id");
        Integer nullableEmpId = resultSet.wasNull() ? null : empId;
        return new UserRecord(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                Role.valueOf(resultSet.getString("role").trim().toUpperCase()),
                resultSet.getInt("status"),
                nullableEmpId
        );
    }

    /**
     * 开发/测试用：统计某个用户名是否存在，便于验证唯一键是否生效。
     */
    public int countByUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sys_user WHERE username = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    /**
     * 预留：管理员在系统内创建普通员工账号时使用（Ming 的员工模块可调用）。
     * 密码以哈希形式入库，绝不保存明文。
     */
    public void insert(String username, String passwordHash, Role role, Integer empId) throws SQLException {
        String sql = "INSERT INTO sys_user (username, password_hash, role, status, emp_id) VALUES (?, ?, ?, 1, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, role.name());
            if (empId == null) {
                statement.setNull(4, Types.INTEGER);
            } else {
                statement.setInt(4, empId);
            }
            statement.executeUpdate();
        }
    }
}
