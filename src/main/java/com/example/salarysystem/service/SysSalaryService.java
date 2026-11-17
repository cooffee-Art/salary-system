package com.example.salarysystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.example.salarysystem.dto.SalarySaveDTO;
import com.example.salarysystem.entity.SysSalary;
import com.example.salarysystem.vo.DeptSalaryReportVO;
import com.example.salarysystem.vo.SalaryGenerateResult;
import com.example.salarysystem.vo.SalaryVO;

import java.util.List;

public interface SysSalaryService extends IService<SysSalary> {

    List<SalaryVO> list(String salaryMonth, Integer empId, String keyword);

    Page<SalaryVO> pageSalaries(long current, long size, String salaryMonth, Integer empId, String keyword);

    SalaryVO getDetail(Integer id);

    SysSalary createSalary(SalarySaveDTO request);

    SysSalary updateSalary(Integer id, SalarySaveDTO request);

    void deleteSalary(Integer id);

    List<DeptSalaryReportVO> departmentReport(String month);

    /**
     * 为所有在职员工生成指定月份的工资。
     * 规则：以员工在该月之前最近一条薪资的 base_pay 作为本月基本工资，奖金/扣款为 0。
     */
    SalaryGenerateResult generate(String month);
}
