package com.salary.model;

import java.math.BigDecimal;

/**
 * 薪资记录模型。
 *
 * <p>对应一条"某员工某月"的薪资数据：基本工资、奖金、扣款由录入方提供，
 * 实发工资由 {@code com.salary.service.SalaryCalculator} 计算后写回。</p>
 *
 * <p>金额统一使用 {@link BigDecimal}，禁止使用 double/float，避免浮点误差。</p>
 */
public class SalaryRecord {

    /** 员工 ID（工号或主键，由调用方决定） */
    private String empId;

    /** 员工姓名 */
    private String empName;

    /** 基本工资 */
    private BigDecimal baseSalary;

    /** 奖金 */
    private BigDecimal bonus;

    /** 扣款 */
    private BigDecimal deduction;

    /** 实发工资（由计算引擎写入） */
    private BigDecimal actualSalary;

    public SalaryRecord() {
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public BigDecimal getDeduction() {
        return deduction;
    }

    public void setDeduction(BigDecimal deduction) {
        this.deduction = deduction;
    }

    public BigDecimal getActualSalary() {
        return actualSalary;
    }

    public void setActualSalary(BigDecimal actualSalary) {
        this.actualSalary = actualSalary;
    }

    /**
     * 便于日志和调试输出；金额为 null 时显示 null，不做隐式转换。
     */
    @Override
    public String toString() {
        return "SalaryRecord{"
                + "empId='" + empId + '\''
                + ", empName='" + empName + '\''
                + ", baseSalary=" + baseSalary
                + ", bonus=" + bonus
                + ", deduction=" + deduction
                + ", actualSalary=" + actualSalary
                + '}';
    }
}
