package com.salary.console.ui;

import com.salary.console.i18n.I18n;

import java.util.Scanner;

/**
 * 控制台输入封装：统一处理提示语、数字解析和非法输入。
 */
public final class InputReader {

    private final Scanner scanner;

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String promptKey) {
        System.out.print(I18n.get(promptKey));
        if (!scanner.hasNextLine()) {
            return "";
        }
        String line = scanner.nextLine();
        // 兼容从文件/管道重定向时出现的 UTF-8 BOM
        if (line.startsWith("\uFEFF")) {
            line = line.substring(1);
        }
        return line.trim();
    }

    /**
     * 读取 [min, max] 范围内的整数，非法输入会重新提示。
     */
    public int readInt(String promptKey, int min, int max) {
        while (true) {
            String line = readLine(promptKey);
            try {
                int value = Integer.parseInt(line);
                if (value < min || value > max) {
                    System.out.println(I18n.get("common.invalidChoice", min, max));
                    continue;
                }
                return value;
            } catch (NumberFormatException exception) {
                System.out.println(I18n.get("common.invalidNumber"));
            }
        }
    }

    public void waitForEnter() {
        System.out.print(I18n.get("common.pressEnter"));
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }

    public void close() {
        scanner.close();
    }
}
