package com.example.salarysystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.salarysystem.entity.SysSalary;
import com.example.salarysystem.vo.SalaryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 薪资 Mapper。
 * 继承 BaseMapper 后，单表 insert / updateById / deleteById / selectById 等直接可用；
 * 只有连表带出员工工号、姓名、部门名称的查询需要自己写 SQL。
 */
@Mapper
public interface SysSalaryMapper extends BaseMapper<SysSalary> {

    /**
     * 薪资列表，可按月份、员工筛选；LEFT JOIN 保证薪资记录即使员工信息缺失也能查出。
     */
    @Select("""
            <script>
            SELECT s.id,
                   s.emp_id,
                   e.emp_no,
                   e.name AS emp_name,
                   d.dept_name,
                   s.salary_month,
                   s.base_pay,
                   s.bonus,
                   s.deduction,
                   s.actual_pay,
                   s.create_time
            FROM sys_salary s
            LEFT JOIN sys_employee e ON e.id = s.emp_id
            LEFT JOIN sys_department d ON d.id = e.dept_id
            <where>
                <if test="salaryMonth != null and salaryMonth != ''">
                    AND s.salary_month = #{salaryMonth}
                </if>
                <if test="empId != null">
                    AND s.emp_id = #{empId}
                </if>
            </where>
            ORDER BY s.id
            </script>
            """)
    List<SalaryVO> selectSalaryList(@Param("salaryMonth") String salaryMonth,
                                    @Param("empId") Integer empId);

    /**
     * 分页 + 条件筛选。Page 参数由分页插件自动识别，自动改写 SQL 并执行 count。
     */
    @Select("""
            <script>
            SELECT s.id,
                   s.emp_id,
                   e.emp_no,
                   e.name AS emp_name,
                   d.dept_name,
                   s.salary_month,
                   s.base_pay,
                   s.bonus,
                   s.deduction,
                   s.actual_pay,
                   s.create_time
            FROM sys_salary s
            LEFT JOIN sys_employee e ON e.id = s.emp_id
            LEFT JOIN sys_department d ON d.id = e.dept_id
            <where>
                <if test="salaryMonth != null and salaryMonth != ''">
                    AND s.salary_month = #{salaryMonth}
                </if>
                <if test="empId != null">
                    AND s.emp_id = #{empId}
                </if>
            </where>
            ORDER BY s.id
            </script>
            """)
    Page<SalaryVO> selectSalaryPage(Page<SalaryVO> page,
                                    @Param("salaryMonth") String salaryMonth,
                                    @Param("empId") Integer empId);

    /** 按主键查询薪资详情（连表带出员工工号、姓名、部门名称），不存在返回 null。 */
    @Select("""
            SELECT s.id,
                   s.emp_id,
                   e.emp_no,
                   e.name AS emp_name,
                   d.dept_name,
                   s.salary_month,
                   s.base_pay,
                   s.bonus,
                   s.deduction,
                   s.actual_pay,
                   s.create_time
            FROM sys_salary s
            LEFT JOIN sys_employee e ON e.id = s.emp_id
            LEFT JOIN sys_department d ON d.id = e.dept_id
            WHERE s.id = #{id}
            """)
    SalaryVO selectDetailById(@Param("id") Integer id);
}
