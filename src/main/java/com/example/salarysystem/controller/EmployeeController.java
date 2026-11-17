package com.example.salarysystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.salarysystem.common.PageResult;
import com.example.salarysystem.common.Result;
import com.example.salarysystem.entity.SysEmployee;
import com.example.salarysystem.dto.EmployeeSaveDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import com.example.salarysystem.service.SysEmployeeService;
import com.example.salarysystem.vo.EmployeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 员工接口。
 *
 * GET /api/employees          全部员工（带部门名）
 * GET /employees              兼容旧地址（新代码请使用 /api/employees）
 * GET /api/employees/page     分页 + 关键字搜索
 * GET /api/employees/{id}     员工详情
 *
 * @CrossOrigin 只用于本地联调，允许 Vite 的 5173/5174 等端口访问。
 * 生产环境应改成网关或全局 CORS 配置，并收紧允许的来源。
 */
@RestController
@RequestMapping({"/api/employees", "/employees"})
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
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Positive Integer deptId,
            @RequestParam(required = false) String deptName) {
        Page<EmployeeVO> page = employeeService.pageEmployees(current, size, keyword, deptId, deptName);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/{id}")
    public Result<SysEmployee> detail(@PathVariable @Positive Integer id) {
        SysEmployee employee = employeeService.getById(id);
        if (employee == null) {
            return Result.error(404, "员工不存在");
        }
        return Result.success(employee);
    }

    @PostMapping
    public Result<SysEmployee> create(@Valid @RequestBody EmployeeSaveDTO request) {
        return Result.success(employeeService.createEmployee(request));
    }

    @PutMapping("/{id}")
    public Result<SysEmployee> update(@PathVariable @Positive Integer id,
                                      @Valid @RequestBody EmployeeSaveDTO request) {
        return Result.success(employeeService.updateEmployee(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable @Positive Integer id) {
        return Result.success(employeeService.deleteEmployee(id));
    }
}
