package com.example.salarysystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.example.salarysystem.dto.SalarySaveDTO;
import com.example.salarysystem.entity.SysSalary;
import com.example.salarysystem.vo.SalaryVO;

import java.util.List;

/**
 * 薪资服务接口。
 * 继承 IService 后，save / updateById / removeById / getById 等标准方法直接可用。
 */
public interface SysSalaryService extends IService<SysSalary> {

    /** 按月份、员工查询薪资列表（带员工工号、姓名、部门名称） */
    List<SalaryVO> list(String salaryMonth, Integer empId);

    /** 分页查询薪资，月份 / 员工为可选筛选条件 */
    Page<SalaryVO> pageSalaries(long current, long size, String salaryMonth, Integer empId);

    /** 薪资详情，不存在抛 404 */
    SalaryVO getDetail(Integer id);

    SysSalary createSalary(SalarySaveDTO request);

    SysSalary updateSalary(Integer id, SalarySaveDTO request);

    void deleteSalary(Integer id);
}
