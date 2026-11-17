package com.example.salarysystem.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 生成工资时使用的“员工 + 历史基本工资”行。
 */
@Data
public class SalaryBaseRow {
    private Integer empId;
    private BigDecimal basePay;
}
