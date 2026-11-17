package com.example.salarysystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 月度薪资记录，对应 salary_db.sys_salary。
 * 唯一约束：同一员工同一月份只能有一条记录。
 */
@Data
@TableName("sys_salary")
public class SysSalary implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer empId;

    /** 格式 yyyy-MM，例如 2024-09 */
    private String salaryMonth;

    private BigDecimal basePay;

    private BigDecimal bonus;

    private BigDecimal deduction;

    private BigDecimal actualPay;

    private LocalDateTime createTime;
}
