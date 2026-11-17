package com.example.salarysystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.salarysystem.common.PageResult;
import com.example.salarysystem.common.Result;
import com.example.salarysystem.dto.SalarySaveDTO;
import com.example.salarysystem.entity.SysSalary;
import com.example.salarysystem.service.SysSalaryService;
import com.example.salarysystem.vo.DeptSalaryReportVO;
import com.example.salarysystem.vo.SalaryGenerateResult;
import com.example.salarysystem.vo.SalaryVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 薪资接口。
 *
 * 列表/分页同时兼容两组参数：
 *   - 旧：salaryMonth、empId
 *   - 新：month、keyword
 * 这样 C 的测试和前端新页面都能用。
 */
@RestController
@RequestMapping({"/api/salaries", "/salaries"})
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, maxAge = 3600)
@RequiredArgsConstructor
public class SalaryController {

    private final SysSalaryService salaryService;

    @GetMapping
    public Result<List<SalaryVO>> list(
            @RequestParam(required = false) String salaryMonth,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Positive Integer empId) {
        return Result.success(salaryService.list(firstNonBlank(salaryMonth, month), empId, keyword));
    }

    @GetMapping("/page")
    public Result<PageResult<SalaryVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String salaryMonth,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Positive Integer empId) {
        Page<SalaryVO> page = salaryService.pageSalaries(
                current, size, firstNonBlank(salaryMonth, month), empId, keyword);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<SalaryVO> detail(@PathVariable @Positive Integer id) {
        return Result.success(salaryService.getDetail(id));
    }

    @PostMapping
    public Result<SysSalary> create(@Valid @RequestBody SalarySaveDTO request) {
        return Result.success(salaryService.createSalary(request));
    }

    @PutMapping("/{id}")
    public Result<SysSalary> update(@PathVariable @Positive Integer id,
                                    @Valid @RequestBody SalarySaveDTO request) {
        return Result.success(salaryService.updateSalary(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Positive Integer id) {
        salaryService.deleteSalary(id);
        return Result.success();
    }

    @GetMapping("/report/department")
    public Result<List<DeptSalaryReportVO>> departmentReport(@RequestParam String month) {
        return Result.success(salaryService.departmentReport(month));
    }

    @PostMapping("/generate")
    public Result<SalaryGenerateResult> generate(@RequestParam String month) {
        return Result.success(salaryService.generate(month));
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }
}
