package com.example.salarysystem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/** 新增和完整修改薪资的请求；不接收主键 id 和后端计算的 actualPay。 */
@Data
public class SalarySaveDTO {

    @NotNull(message = "员工不能为空")
    @Positive(message = "员工ID必须为正整数")
    private Integer empId;

    @NotBlank(message = "薪资月份不能为空")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "月份格式应为 yyyy-MM，如 2024-09")
    private String salaryMonth;

    @NotNull(message = "基本工资不能为空")
    @DecimalMin(value = "0.00", message = "基本工资不能为负数")
    private BigDecimal basePay;

    @DecimalMin(value = "0.00", message = "奖金不能为负数")
    private BigDecimal bonus;

    @DecimalMin(value = "0.00", message = "扣款不能为负数")
    private BigDecimal deduction;

    public void setSalaryMonth(String salaryMonth) {
        this.salaryMonth = salaryMonth == null || salaryMonth.isBlank() ? null : salaryMonth.strip();
    }
}
