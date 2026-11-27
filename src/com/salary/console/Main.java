package com.salary.console;

import com.salary.console.service.AuthServiceFactory;
import com.salary.console.ui.InputReader;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 纯 Java 控制台入口。
 *
 * 登录实现由 AuthServiceFactory 决定：
 * 数据库可用时走 JdbcAuthService，不可用时退回内存演示账号，保证程序总能启动。
 *
 * 运行：
 *   java -Dfile.encoding=UTF-8 -cp "out;resources;lib/*" com.salary.console.Main
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            InputReader input = new InputReader(scanner);
            new ConsoleApplication(input, AuthServiceFactory.create()).run();
        }
    }
}
