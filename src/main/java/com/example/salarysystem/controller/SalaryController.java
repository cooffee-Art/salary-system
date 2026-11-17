package com.example.salarysystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.salarysystem.common.PageResult;
import com.example.salarysystem.common.Result;
import com.example.salarysystem.service.SysSalaryService;
import com.example.salarysystem.vo.DeptSalaryReportVO;
import com.example.salarysystem.vo.SalaryGenerateResult;
import com.example.salarysystem.vo.SalaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/salaries")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, maxAge = 3600)
@RequiredArgsConstructor
public class SalaryController {

    private final SysSalaryService salaryService;

    /**
     * 分页查询薪资，可按月份、姓名/工号筛选。
     * GET /api/salaries/page?month=2024-09&keyword=张&current=1&size=10
     */
    @GetMapping("/page")
    public Result<PageResult<SalaryVO>> page(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<SalaryVO> page = salaryService.page(month, keyword, current, size);
        return Result.success(PageResult.of(page));
    }

    /**
     * 部门月度薪资汇总报表。
     * GET /api/salaries/report/department?month=2024-09
     */
    @GetMapping("/report/department")
    public Result<List<DeptSalaryReportVO>> departmentReport(@RequestParam String month) {
        return Result.success(salaryService.departmentReport(month));
    }

    /**
     * 批量生成月度工资（事务 + 唯一约束幂等）。
     * POST /api/salaries/generate?month=2024-10
     */
    @PostMapping("/generate")
    public Result<SalaryGenerateResult> generate(@RequestParam String month) {
        return Result.success(salaryService.generate(month));
    }
}
