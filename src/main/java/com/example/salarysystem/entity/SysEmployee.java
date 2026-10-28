package com.example.salarysystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 员工实体，对应 salary_db.sys_employee。
 *
 * 字段与表结构的对应关系：
 * id        -> id
 * empNo     -> emp_no
 * deptId    -> dept_id
 * hireDate  -> hire_date
 * 其余同名。
 */
@Data
@TableName("sys_employee")
public class SysEmployee implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 工号，唯一 */
    private String empNo;

    /** 姓名 */
    private String name;

    /** 性别：男 / 女 */
    private String gender;

    /** 手机号 */
    private String phone;

    /** 部门 ID，对应 sys_department.id */
    private Integer deptId;

    /** 入职日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    /** 状态：1=在职，0=离职 */
    private Integer status;
}
