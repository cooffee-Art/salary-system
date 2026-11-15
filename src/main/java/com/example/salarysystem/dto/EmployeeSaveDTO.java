package com.example.salarysystem.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/** 新增和完整修改员工的请求；不接受数据库主键。 */
@Data
public class EmployeeSaveDTO {
    @NotBlank(message = "工号不能为空")
    @Size(max = 20, message = "工号最多20个字符")
    private String empNo;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名最多50个字符")
    private String name;

    @Pattern(regexp = "男|女", message = "性别只能是男或女")
    private String gender;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Positive(message = "部门ID必须为正整数")
    private Integer deptId;

    @PastOrPresent(message = "入职日期不能晚于今天")
    private LocalDate hireDate;

    @Min(value = 0, message = "状态只能是0或1")
    @Max(value = 1, message = "状态只能是0或1")
    private Integer status;

    public void setEmpNo(String empNo) { this.empNo = normalize(empNo); }
    public void setName(String name) { this.name = normalize(name); }
    public void setGender(String gender) { this.gender = normalize(gender); }
    public void setPhone(String phone) { this.phone = normalize(phone); }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
