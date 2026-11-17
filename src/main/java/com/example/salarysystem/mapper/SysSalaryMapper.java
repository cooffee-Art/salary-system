package com.example.salarysystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.salarysystem.entity.SysSalary;
import com.example.salarysystem.vo.DeptSalaryReportVO;
import com.example.salarysystem.vo.SalaryBaseRow;
import com.example.salarysystem.vo.SalaryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysSalaryMapper extends BaseMapper<SysSalary> {

    /**
     * 薪资列表 / 分页：员工、部门、薪资三表连查。
     * LEFT JOIN 保证员工或部门信息缺失时，薪资记录仍然能查出来。
     */
    @Select("""
            <script>
            SELECT s.id,
                   s.emp_id,
                   e.emp_no,
                   e.name AS emp_name,
                   e.dept_id,
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
                <if test="keyword != null and keyword != ''">
                    AND (e.name LIKE CONCAT('%', #{keyword}, '%')
                         OR e.emp_no LIKE CONCAT('%', #{keyword}, '%'))
                </if>
            </where>
            ORDER BY s.id
            </script>
            """)
    List<SalaryVO> selectSalaryList(@Param("salaryMonth") String salaryMonth,
                                    @Param("empId") Integer empId,
                                    @Param("keyword") String keyword);

    @Select("""
            <script>
            SELECT s.id,
                   s.emp_id,
                   e.emp_no,
                   e.name AS emp_name,
                   e.dept_id,
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
                <if test="keyword != null and keyword != ''">
                    AND (e.name LIKE CONCAT('%', #{keyword}, '%')
                         OR e.emp_no LIKE CONCAT('%', #{keyword}, '%'))
                </if>
            </where>
            ORDER BY s.id
            </script>
            """)
    Page<SalaryVO> selectSalaryPage(Page<SalaryVO> page,
                                    @Param("salaryMonth") String salaryMonth,
                                    @Param("empId") Integer empId,
                                    @Param("keyword") String keyword);

    @Select("""
            SELECT s.id,
                   s.emp_id,
                   e.emp_no,
                   e.name AS emp_name,
                   e.dept_id,
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

    /**
     * 部门月度薪资汇总。
     */
    @Select("""
            SELECT d.id AS dept_id,
                   d.dept_name,
                   s.salary_month,
                   COUNT(DISTINCT e.id) AS emp_count,
                   COALESCE(SUM(s.base_pay), 0) AS total_base,
                   COALESCE(SUM(s.bonus), 0) AS total_bonus,
                   COALESCE(SUM(s.deduction), 0) AS total_deduction,
                   COALESCE(SUM(s.actual_pay), 0) AS total_actual
            FROM sys_salary s
            JOIN sys_employee e ON e.id = s.emp_id
            JOIN sys_department d ON d.id = e.dept_id
            WHERE s.salary_month = #{month}
            GROUP BY d.id, d.dept_name, s.salary_month
            ORDER BY total_actual DESC
            """)
    List<DeptSalaryReportVO> selectDepartmentReport(@Param("month") String month);

    @Select("SELECT COUNT(*) FROM sys_salary WHERE salary_month = #{month}")
    long countByMonth(@Param("month") String month);

    /**
     * 取每个在职员工在目标月份之前的最近一条基本工资，作为生成新月份工资的基数。
     * LEFT JOIN 让没有历史薪资的员工也返回（base_pay 为 null），由 Service 计入 skipped。
     */
    @Select("""
            SELECT e.id AS emp_id,
                   latest.base_pay
            FROM sys_employee e
            LEFT JOIN (
                SELECT s1.emp_id, s1.base_pay
                FROM sys_salary s1
                JOIN (
                    SELECT emp_id, MAX(salary_month) AS max_month
                    FROM sys_salary
                    WHERE salary_month < #{month}
                    GROUP BY emp_id
                ) m ON m.emp_id = s1.emp_id AND m.max_month = s1.salary_month
            ) latest ON latest.emp_id = e.id
            WHERE e.status = 1
            ORDER BY e.id
            """)
    List<SalaryBaseRow> selectPayrollBases(@Param("month") String month);
}
