package com.example.salarysystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 员工列表展示对象：在员工表字段之外，多带一个部门名称。
 * 这样前端表格可以直接显示部门，不用再发一次请求做映射。
 */
@Data
public class EmployeeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String empNo;

    private String name;

    private String gender;

    private String phone;

    private Integer deptId;

    /** 连表查询得到的部门名称 */
    private String deptName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    /** 1=在职，0=离职 */
    private Integer status;
}
