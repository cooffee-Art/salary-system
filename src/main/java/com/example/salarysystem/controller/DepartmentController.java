package com.example.salarysystem.controller;

import com.example.salarysystem.common.Result;
import com.example.salarysystem.entity.SysDepartment;
import com.example.salarysystem.service.SysDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, maxAge = 3600)
@RequiredArgsConstructor
public class DepartmentController {
    private final SysDepartmentService departmentService;

    @GetMapping
    public Result<List<SysDepartment>> list() {
        return Result.success(departmentService.listDepartments());
    }
}
