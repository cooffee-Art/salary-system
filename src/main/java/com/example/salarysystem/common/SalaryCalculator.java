package com.example.salarysystem.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 薪资计算纯 Java 核心，不依赖任何 Spring / MyBatis-Plus 框架。
 *
 * 计算口径：实发工资 = 基本工资 + 奖金 - 扣款，结果保留两位小数（四舍五入）。
 * 抽成静态工具类的目的：金额规则可以脱离容器单独做单元测试，Service 层只负责调用和转业务异常。
 */
public final class SalaryCalculator {

    private SalaryCalculator() {
    }

    /**
     * 计算实发工资。
     *
     * @param basePay   基本工资，不能为 null
     * @param bonus     奖金，不能为 null
     * @param deduction 扣款，不能为 null
     * @return 实发工资，保留两位小数（四舍五入）
     */
    public static BigDecimal actualPay(BigDecimal basePay, BigDecimal bonus, BigDecimal deduction) {
        if (basePay == null || bonus == null || deduction == null) {
            throw new IllegalArgumentException("基本工资、奖金、扣款均不能为空");
        }
        BigDecimal total = basePay.add(bonus).subtract(deduction);
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 校验薪资录入是否合法。
     *
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
