package com.example.salarysystem.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 薪资计算纯 Java 核心，不依赖 Spring / MyBatis-Plus。
 *
 * 实发工资 = 基本工资 + 奖金 - 扣款，保留两位小数（四舍五入）。
 * 抽成静态工具类后，金额规则可以脱离容器单独测试。
 */
public final class SalaryCalculator {

    private SalaryCalculator() {
    }

    public static BigDecimal actualPay(BigDecimal basePay, BigDecimal bonus, BigDecimal deduction) {
        if (basePay == null || bonus == null || deduction == null) {
            throw new IllegalArgumentException("基本工资、奖金、扣款均不能为空");
        }
        return basePay.add(bonus).subtract(deduction).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @return 校验通过返回 null；不合法返回中文错误说明
     */
    public static String validate(BigDecimal basePay, BigDecimal bonus, BigDecimal deduction) {
        if (basePay == null || basePay.signum() < 0) {
            return "基本工资不能为负数";
        }
        if (bonus == null || bonus.signum() < 0) {
            return "奖金不能为负数";
        }
        if (deduction == null || deduction.signum() < 0) {
            return "扣款不能为负数";
        }
        if (basePay.add(bonus).compareTo(deduction) < 0) {
            return "扣款不能大于基本工资与奖金之和";
        }
        return null;
    }
}
