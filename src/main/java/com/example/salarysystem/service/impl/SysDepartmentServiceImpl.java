package com.example.salarysystem.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.salarysystem.entity.SysDepartment;
import com.example.salarysystem.mapper.SysDepartmentMapper;
import com.example.salarysystem.service.SysDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDepartmentServiceImpl implements SysDepartmentService {
    private final SysDepartmentMapper departmentMapper;

    @Override
    public List<SysDepartment> listDepartments() {
        return departmentMapper.selectList(Wrappers.<SysDepartment>lambdaQuery()
                .orderByAsc(SysDepartment::getId));
    }
}
