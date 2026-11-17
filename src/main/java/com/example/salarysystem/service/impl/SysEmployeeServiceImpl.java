package com.example.salarysystem.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.salarysystem.common.BusinessException;
import com.example.salarysystem.dto.EmployeeSaveDTO;
import com.example.salarysystem.entity.SysEmployee;
import com.example.salarysystem.mapper.SysEmployeeMapper;
import com.example.salarysystem.mapper.SysDepartmentMapper;
import com.example.salarysystem.service.SysEmployeeService;
import com.example.salarysystem.vo.EmployeeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 员工服务实现。
 * 继承 ServiceImpl 后，baseMapper 已经注入好，直接用即可。
 */
@Service
@RequiredArgsConstructor
public class SysEmployeeServiceImpl extends ServiceImpl<SysEmployeeMapper, SysEmployee>
        implements SysEmployeeService {

    private final SysDepartmentMapper departmentMapper;

    @Override
    public List<EmployeeVO> listAll() {
        return baseMapper.selectEmployeeList();
    }

    @Override
    public Page<EmployeeVO> pageEmployees(long current, long size, String keyword, Integer deptId, String deptName) {
        // 兜底保护：页码至少为 1，每页最多 100 条，防止前端传 100000 把数据库拖死
        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        Page<EmployeeVO> page = new Page<>(safeCurrent, safeSize);
        String safeKeyword = keyword == null ? null : keyword.trim();
        return baseMapper.selectEmployeePage(page, safeKeyword, deptId,
                deptName == null ? null : deptName.trim());
    }

    @Override
    @Transactional
    public SysEmployee createEmployee(EmployeeSaveDTO request) {
        validateReferences(request, null);
        SysEmployee employee = toEntity(request);
        baseMapper.insert(employee);
        return employee;
    }

    @Override
    @Transactional
    public SysEmployee updateEmployee(Integer id, EmployeeSaveDTO request) {
        requireEmployee(id);
        validateReferences(request, id);
        SysEmployee employee = toEntity(request);
        employee.setId(id);
        // PUT 完整替换：显式 set 允许清空部门、手机号和入职日期。
        baseMapper.update(null, Wrappers.<SysEmployee>lambdaUpdate()
                .eq(SysEmployee::getId, id)
                .set(SysEmployee::getEmpNo, employee.getEmpNo())
                .set(SysEmployee::getName, employee.getName())
                .set(SysEmployee::getGender, employee.getGender())
                .set(SysEmployee::getPhone, employee.getPhone())
                .set(SysEmployee::getDeptId, employee.getDeptId())
                .set(SysEmployee::getHireDate, employee.getHireDate())
                .set(SysEmployee::getStatus, employee.getStatus()));
        return employee;
    }

    @Override
    @Transactional
    public String deleteEmployee(Integer id) {
        requireEmployee(id);
        if (baseMapper.countSalaryRecords(id) > 0) {
            baseMapper.update(null, Wrappers.<SysEmployee>lambdaUpdate()
                    .eq(SysEmployee::getId, id).set(SysEmployee::getStatus, 0));
            return "已转为离职，保留薪资记录";
        }
        baseMapper.deleteById(id);
        return "员工已删除";
    }

    private void requireEmployee(Integer id) {
        if (baseMapper.selectForUpdate(id) == null) {
            throw new BusinessException(404, "员工不存在");
        }
    }

    private void validateReferences(EmployeeSaveDTO request, Integer excludedId) {
        Long duplicates = baseMapper.selectCount(Wrappers.<SysEmployee>lambdaQuery()
                .eq(SysEmployee::getEmpNo, request.getEmpNo())
                .ne(excludedId != null, SysEmployee::getId, excludedId));
        if (duplicates > 0) {
            throw new BusinessException(400, "工号已存在");
        }
        if (request.getDeptId() != null && departmentMapper.selectById(request.getDeptId()) == null) {
            throw new BusinessException(400, "部门不存在");
        }
    }

    private SysEmployee toEntity(EmployeeSaveDTO request) {
        SysEmployee employee = new SysEmployee();
        employee.setEmpNo(request.getEmpNo());
        employee.setName(request.getName());
        employee.setGender(request.getGender());
        employee.setPhone(request.getPhone());
        employee.setDeptId(request.getDeptId());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        return employee;
    }
}
