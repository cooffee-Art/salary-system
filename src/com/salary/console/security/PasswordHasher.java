package com.salary.console.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * 密码哈希工具，只依赖 JDK 自带的 javax.crypto，不引入任何第三方库。
 *
 * 算法：PBKDF2WithHmacSHA256，随机盐，可配置迭代次数。
 * 存储格式：pbkdf2$sha256$迭代次数$盐(Base64)$哈希(Base64)
 *
 * 数据库里不保存明文密码；登录时用 verify 比对哈希。
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2$sha256$";
    private static final int DEFAULT_ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    /**
     * 生成可入库的密码哈希串。
     */
    public static String hash(String rawPassword) {
        return hash(rawPassword, DEFAULT_ITERATIONS);
    }

    public static String hash(String rawPassword, int iterations) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("password must not be empty");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] digest = pbkdf2(rawPassword, salt, iterations);
        return PREFIX + iterations + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(digest);
    }

    /**
     * 校验明文密码与库中的哈希串是否匹配。
     * 哈希串格式非法时返回 false，不让异常冒到菜单层。
     */
    public static boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length != 5 || !"pbkdf2".equals(parts[0]) || !"sha256".equals(parts[1])) {
            return false;
        }

        int iterations;
        try {
            iterations = Integer.parseInt(parts[2]);
        } catch (NumberFormatException exception) {
            return false;
        }

        byte[] salt;
        byte[] expected;
        try {
            salt = Base64.getDecoder().decode(parts[3]);
            expected = Base64.getDecoder().decode(parts[4]);
        } catch (IllegalArgumentException exception) {
            return false;
        }

        byte[] actual = pbkdf2(rawPassword, salt, iterations);
        // 恒定时间比较，避免通过响应时间猜测密码
        return MessageDigest.isEqual(expected, actual);
    }

    private static byte[] pbkdf2(String rawPassword, byte[] salt, int iterations) {
        char[] chars = rawPassword.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new IllegalStateException("PBKDF2 is not available in this JDK", exception);
        } finally {
            spec.clearPassword();
            java.util.Arrays.fill(chars, '\0');
        }
    }

    /**
     * 开发期小工具：生成密码哈希，便于写入 sql/init.sql。
     * 用法：java -cp out com.salary.console.security.PasswordHasher Admin@123
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("usage: PasswordHasher <rawPassword> [iterations]");
            return;
        }
        int iterations = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_ITERATIONS;
        System.out.println(hash(args[0], iterations));
    }
}
