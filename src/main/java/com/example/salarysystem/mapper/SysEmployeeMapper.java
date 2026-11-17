package com.example.salarysystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.salarysystem.entity.SysEmployee;
import com.example.salarysystem.vo.EmployeeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工 Mapper。
 * 继承 BaseMapper 后，单表 CRUD 全部免费获得：
 * insert / deleteById / updateById / selectById / selectList ...
 * 只有连表查询才需要自己写 SQL。
 */
@Mapper
public interface SysEmployeeMapper extends BaseMapper<SysEmployee> {

    /**
     * 查询全部员工（带部门名称）。
     * 用 LEFT JOIN 而不是 INNER JOIN：即使员工没有分配部门，也要能查出来。
     */
    @Select("""
            SELECT e.id,
                   e.emp_no,
                   e.name,
                   e.gender,
                   e.phone,
                   e.dept_id,
                   d.dept_name,
                   e.hire_date,
                   e.status
            FROM sys_employee e
            LEFT JOIN sys_department d ON d.id = e.dept_id
            ORDER BY e.id
            """)
    List<EmployeeVO> selectEmployeeList();

    /**
     * 分页 + 关键字搜索（姓名或工号）。
     * Page 参数由 MyBatis-Plus 分页插件自动识别，会自动改写 SQL 并执行 count。
     */
    @Select("""
            <script>
            SELECT e.id,
                   e.emp_no,
                   e.name,
                   e.gender,
                   e.phone,
                   e.dept_id,
                   d.dept_name,
                   e.hire_date,
                   e.status
            FROM sys_employee e
            LEFT JOIN sys_department d ON d.id = e.dept_id
            <where>
                <if test="keyword != null and keyword != ''">
                    AND (e.name LIKE CONCAT('%', #{keyword}, '%')
                         OR e.emp_no LIKE CONCAT('%', #{keyword}, '%'))
                </if>
                <if test="deptId != null">
                    AND e.dept_id = #{deptId}
                </if>
                <if test="deptName != null and deptName != ''">
                    AND d.dept_name LIKE CONCAT('%', #{deptName}, '%')
                </if>
            </where>
            ORDER BY e.id
            </script>
            """)
    Page<EmployeeVO> selectEmployeePage(Page<EmployeeVO> page, @Param("keyword") String keyword,
                                       @Param("deptId") Integer deptId, @Param("deptName") String deptName);

    /** 锁住员工行，避免删除检查期间新增薪资记录产生竞态；必须在事务内调用。 */
    @Select("SELECT * FROM sys_employee WHERE id = #{id} FOR UPDATE")
    SysEmployee selectForUpdate(@Param("id") Integer id);

    @Select("SELECT COUNT(*) FROM sys_salary WHERE emp_id = #{id}")
    long countSalaryRecords(@Param("id") Integer id);
}
