package com.example.salarysystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SalaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer empId;
    private String empNo;
    private String empName;
    private Integer deptId;
    private String deptName;
    private String salaryMonth;
    private BigDecimal basePay;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private BigDecimal actualPay;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
