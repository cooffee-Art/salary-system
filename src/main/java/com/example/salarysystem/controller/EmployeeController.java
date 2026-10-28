package com.example.salarysystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.salarysystem.common.PageResult;
import com.example.salarysystem.common.Result;
import com.example.salarysystem.entity.SysEmployee;
import com.example.salarysystem.service.SysEmployeeService;
import com.example.salarysystem.vo.EmployeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 员工接口。
 *
 * GET /api/employees          全部员工（带部门名）
 * GET /api/employees/page     分页 + 关键字搜索
 * GET /api/employees/{id}     员工详情
 *
 * @CrossOrigin 只用于本地联调，允许 Vite 的 5173/5174 等端口访问。
 * 生产环境应改成网关或全局 CORS 配置，并收紧允许的来源。
 */
@RestController
@RequestMapping("/api/employees")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, maxAge = 3600)
@RequiredArgsConstructor
public class EmployeeController {

    private final SysEmployeeService employeeService;

    @GetMapping
    public Result<List<EmployeeVO>> list() {
        return Result.success(employeeService.listAll());
    }

    @GetMapping("/page")
    public Result<PageResult<EmployeeVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        Page<EmployeeVO> page = employeeService.pageEmployees(current, size, keyword);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<SysEmployee> detail(@PathVariable Integer id) {
        SysEmployee employee = employeeService.getById(id);
        if (employee == null) {
            return Result.error(404, "员工不存在");
        }
        return Result.success(employee);
    }
}
