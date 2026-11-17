package com.example.salarysystem.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 一次批量生成工资的结果。
 */
public record SalaryGenerateResult(
        String month,
        int generated,
        int skipped,
        BigDecimal totalActual
) implements Serializable {
}
