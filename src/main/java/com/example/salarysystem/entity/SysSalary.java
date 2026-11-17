package com.example.salarysystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 薪资记录实体，对应 salary_db.sys_salary。
 *
 * 字段与表结构对应：
 * id           -> id
 * empId        -> emp_id
 * salaryMonth  -> salary_month
 * basePay      -> base_pay
 * bonus        -> bonus
 * deduction    -> deduction
 * actualPay    -> actual_pay
 * createTime   -> create_time
 */
@Data
@TableName("sys_salary")
public class SysSalary implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 员工 ID，对应 sys_employee.id */
    private Integer empId;

    /** 薪资月份，格式 yyyy-MM，如 2024-09 */
    private String salaryMonth;

    /** 基本工资 */
    private BigDecimal basePay;

    /** 奖金 */
    private BigDecimal bonus;

    /** 扣款 */
    private BigDecimal deduction;

    /** 实发工资 = 基本工资 + 奖金 - 扣款，由后端计算，不信任前端传值 */
    private BigDecimal actualPay;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
