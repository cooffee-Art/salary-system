package com.example.salarysystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.salarysystem.vo.DeptSalaryReportVO;
import com.example.salarysystem.vo.SalaryGenerateResult;
import com.example.salarysystem.vo.SalaryVO;

import java.util.List;

public interface SysSalaryService {

    Page<SalaryVO> page(String month, String keyword, long current, long size);

    List<DeptSalaryReportVO> departmentReport(String month);

    /**
     * 为所有在职员工生成指定月份的工资。
     * 规则：以员工在该月之前最近一条薪资的 base_pay 作为本月基本工资，奖金/扣款为 0。
     */
    SalaryGenerateResult generate(String month);
}
