package com.example.salarysystem.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DeptSalaryReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer deptId;
    private String deptName;
    private String salaryMonth;
    private Integer empCount;
    private BigDecimal totalBase;
    private BigDecimal totalBonus;
    private BigDecimal totalDeduction;
    private BigDecimal totalActual;
}
