package com.salary.service;

import com.salary.model.SalaryRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 薪资计算引擎。
 *
 * <p>业务规则（与 README 保持一致）：</p>
 * <ol>
 *   <li>实发工资 = 基本工资 + 奖金 - 扣款；</li>
 *   <li>所有金额计算保留两位小数，采用 {@link RoundingMode#HALF_UP} 四舍五入；</li>
 *   <li>基本工资、奖金、扣款不能为 null，也不能为负数；</li>
 *   <li>扣款不能大于（基本工资 + 奖金）。</li>
 * </ol>
 *
 * <p>校验失败时抛出 {@link IllegalArgumentException}，消息为中文，
 * 由上层（控制台菜单 / 国际化层）统一捕获并转换成对应语言提示。</p>
 */
public class SalaryCalculator {

    /** 金额统一保留的小数位数 */
    private static final int MONEY_SCALE = 2;

    /** 金额舍入模式：四舍五入 */
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * 计算实发工资并写回 {@code record.actualSalary}。
     *
     * @param record 待计算的薪资记录，不能为 null
     * @throws IllegalArgumentException 当记录为空、金额为空/为负，或扣款超过总工资时
     */
    public void calculate(SalaryRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("薪资记录不能为空");
        }

        BigDecimal baseSalary = requireNonNegative(record.getBaseSalary(), "基本工资");
        BigDecimal bonus = requireNonNegative(record.getBonus(), "奖金");
        BigDecimal deduction = requireNonNegative(record.getDeduction(), "扣款");

        BigDecimal grossSalary = baseSalary.add(bonus);
        if (deduction.compareTo(grossSalary) > 0) {
            throw new IllegalArgumentException("扣款金额不能大于总工资");
        }

        BigDecimal actualSalary = grossSalary
                .subtract(deduction)
                .setScale(MONEY_SCALE, ROUNDING_MODE);

        record.setActualSalary(actualSalary);
    }

    /**
     * 校验金额非空且非负。
     *
     * @param value     金额
     * @param fieldName 字段中文名，用于拼装异常消息
     * @return 原金额（便于链式使用）
     */
    private static BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + "不能为负数");
        }
        return value;
    }
}
