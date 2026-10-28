package com.example.salarysystem.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.salarysystem.entity.SysEmployee;
import com.example.salarysystem.mapper.SysEmployeeMapper;
import com.example.salarysystem.service.SysEmployeeService;
import com.example.salarysystem.vo.EmployeeVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 员工服务实现。
 * 继承 ServiceImpl 后，baseMapper 已经注入好，直接用即可。
 */
@Service
public class SysEmployeeServiceImpl extends ServiceImpl<SysEmployeeMapper, SysEmployee>
        implements SysEmployeeService {

    @Override
    public List<EmployeeVO> listAll() {
        return baseMapper.selectEmployeeList();
    }

    @Override
    public Page<EmployeeVO> pageEmployees(long current, long size, String keyword) {
        // 兜底保护：页码至少为 1，每页最多 100 条，防止前端传 100000 把数据库拖死
        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        Page<EmployeeVO> page = new Page<>(safeCurrent, safeSize);
        String safeKeyword = keyword == null ? null : keyword.trim();
        return baseMapper.selectEmployeePage(page, safeKeyword);
    }
}
