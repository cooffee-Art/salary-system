package com.example.salarysystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 薪资列表 / 详情展示对象：在薪资表字段之外，连表带出员工工号、姓名和部门名称，
 * 前端表格可以直接展示，不用再发请求做映射。
 */
@Data
public class SalaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer empId;

    /** 员工工号，连表查询得到 */
    private String empNo;

    /** 员工姓名，连表查询得到 */
    private String empName;

    /** 部门名称，连表查询得到 */
    private String deptName;

    private String salaryMonth;

    private BigDecimal basePay;

    private BigDecimal bonus;

    private BigDecimal deduction;

    private BigDecimal actualPay;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
