package com.example.salarysystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.salarysystem.common.PageResult;
import com.example.salarysystem.common.Result;
import com.example.salarysystem.dto.SalarySaveDTO;
import com.example.salarysystem.entity.SysSalary;
import com.example.salarysystem.service.SysSalaryService;
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
 * GET  /api/salaries          薪资列表（可按月份、员工筛选，带员工姓名/工号/部门）
 * GET  /api/salaries/page     分页查询
 * GET  /api/salaries/{id}     薪资详情
 * POST /api/salaries          新增薪资（实发工资由后端重算）
 * PUT  /api/salaries/{id}     修改薪资
 * DELETE /api/salaries/{id}   删除薪资
 *
 * @CrossOrigin 只用于本地联调，允许 Vite 的 5173/5174 等端口访问。
 */
@RestController
@RequestMapping({"/api/salaries", "/salaries"})
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, maxAge = 3600)
@RequiredArgsConstructor
public class SalaryController {

    private final SysSalaryService salaryService;

    @GetMapping
    public Result<List<SalaryVO>> list(@RequestParam(required = false) String salaryMonth,
                                       @RequestParam(required = false) @Positive Integer empId) {
        return Result.success(salaryService.list(salaryMonth, empId));
    }

    @GetMapping("/page")
    public Result<PageResult<SalaryVO>> page(@RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String salaryMonth,
                                             @RequestParam(required = false) @Positive Integer empId) {
        Page<SalaryVO> page = salaryService.pageSalaries(current, size, salaryMonth, empId);
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
}
