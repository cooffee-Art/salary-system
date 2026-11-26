package com.salary.util;

import com.salary.model.SalaryRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * 工资条格式化工具。
 *
 * <p>使用 {@link String#format} 输出带边框、对齐的文本工资条，
 * 金额带千分位并保留两位小数（如 {@code 12,345.67}）。</p>
 *
 * <p>说明：中文在终端中占两个字符宽度，若直接使用 {@code %-10s}
 * 会因 {@code String.length()} 按 char 计数而错位，因此这里按
 * "显示宽度" 计算补空格。</p>
 */
public final class SalarySlipFormatter {

    /** 左侧项目名列宽度（按显示宽度计） */
    private static final int LABEL_WIDTH = 12;

    /** 右侧金额列宽度（按显示宽度计） */
    private static final int VALUE_WIDTH = 18;

    /** 标题内容，居中显示 */
    private static final String TITLE = "工资条 / SALARY SLIP";

    private SalarySlipFormatter() {
        // 工具类，禁止实例化
    }

    /**
     * 格式化单条薪资记录为文本工资条。
     *
     * @param record 薪资记录，不能为 null
     * @return 可直接 {@code System.out.println} 的多行字符串
     * @throws IllegalArgumentException 当 record 为 null 时
     */
    public static String format(SalaryRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("薪资记录不能为空");
        }

        StringBuilder builder = new StringBuilder();
        builder.append(border()).append(System.lineSeparator());
        builder.append(titleRow()).append(System.lineSeparator());
        builder.append(border()).append(System.lineSeparator());

        builder.append(row("工号", text(record.getEmpId()))).append(System.lineSeparator());
        builder.append(row("姓名", text(record.getEmpName()))).append(System.lineSeparator());

        builder.append(border()).append(System.lineSeparator());
        builder.append(row("基本工资", money(record.getBaseSalary()))).append(System.lineSeparator());
        builder.append(row("奖金", money(record.getBonus()))).append(System.lineSeparator());
        builder.append(row("扣款", money(record.getDeduction()))).append(System.lineSeparator());

        builder.append(border()).append(System.lineSeparator());
        builder.append(row("实发工资", money(record.getActualSalary()))).append(System.lineSeparator());
        builder.append(border());

        return builder.toString();
    }

    // ------------------------------------------------------------------
    // 行拼装
    // ------------------------------------------------------------------

    private static String titleRow() {
        int innerWidth = LABEL_WIDTH + 3 + VALUE_WIDTH;
        return String.format("| %s |", center(TITLE, innerWidth));
    }

    private static String row(String label, String value) {
        return String.format(
                "| %s | %s |",
                padRight(label, LABEL_WIDTH),
                padLeft(value, VALUE_WIDTH));
    }

    private static String border() {
        return "+"
                + "-".repeat(LABEL_WIDTH + 2)
                + "+"
                + "-".repeat(VALUE_WIDTH + 2)
                + "+";
    }

    // ------------------------------------------------------------------
    // 金额与文本
    // ------------------------------------------------------------------

    /** 金额格式：千分位 + 两位小数，例如 12345.67 → 12,345.67 */
    private static String money(BigDecimal value) {
        if (value == null) {
            return "-";
        }
        BigDecimal scaled = value.setScale(2, RoundingMode.HALF_UP);
        return String.format(Locale.US, "%,.2f", scaled);
    }

    private static String text(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    // ------------------------------------------------------------------
    // 显示宽度对齐（中文 / 全角算 2 列）
    // ------------------------------------------------------------------

    private static String padRight(String value, int width) {
        int padding = width - displayWidth(value);
        return padding <= 0 ? value : value + " ".repeat(padding);
    }

    private static String padLeft(String value, int width) {
        int padding = width - displayWidth(value);
        return padding <= 0 ? value : " ".repeat(padding) + value;
    }

    private static String center(String value, int width) {
        int padding = width - displayWidth(value);
        if (padding <= 0) {
            return value;
        }
        int left = padding / 2;
        int right = padding - left;
        return " ".repeat(left) + value + " ".repeat(right);
    }

    private static int displayWidth(String value) {
        if (value == null) {
            return 0;
        }
        int width = 0;
        for (int i = 0; i < value.length(); ) {
            int codePoint = value.codePointAt(i);
            width += isWide(codePoint) ? 2 : 1;
            i += Character.charCount(codePoint);
        }
        return width;
    }

    /** 判断是否为东亚宽字符（CJK、假名、韩文、全角符号等） */
    private static boolean isWide(int codePoint) {
        return (codePoint >= 0x1100 && codePoint <= 0x115F)
                || (codePoint >= 0x2E80 && codePoint <= 0xA4CF)
                || (codePoint >= 0xAC00 && codePoint <= 0xD7A3)
                || (codePoint >= 0xF900 && codePoint <= 0xFAFF)
                || (codePoint >= 0xFE30 && codePoint <= 0xFE6F)
                || (codePoint >= 0xFF00 && codePoint <= 0xFF60)
                || (codePoint >= 0xFFE0 && codePoint <= 0xFFE6)
                || (codePoint >= 0x20000 && codePoint <= 0x3FFFD);
    }
}
