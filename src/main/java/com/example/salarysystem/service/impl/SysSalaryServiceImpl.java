package com.example.salarysystem.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.salarysystem.common.BusinessException;
import com.example.salarysystem.common.SalaryCalculator;
import com.example.salarysystem.dto.SalarySaveDTO;
import com.example.salarysystem.entity.SysEmployee;
import com.example.salarysystem.entity.SysSalary;
import com.example.salarysystem.mapper.SysEmployeeMapper;
import com.example.salarysystem.mapper.SysSalaryMapper;
import com.example.salarysystem.service.SysSalaryService;
import com.example.salarysystem.vo.SalaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 薪资服务实现。
 * 继承 ServiceImpl 后 baseMapper 已注入；实发工资一律由后端重算，不信任前端传入。
 */
@Service
@RequiredArgsConstructor
public class SysSalaryServiceImpl extends ServiceImpl<SysSalaryMapper, SysSalary>
        implements SysSalaryService {

    private final SysEmployeeMapper employeeMapper;

    @Override
    public List<SalaryVO> list(String salaryMonth, Integer empId) {
        return baseMapper.selectSalaryList(normalizeMonth(salaryMonth), empId);
    }

    @Override
    public Page<SalaryVO> pageSalaries(long current, long size, String salaryMonth, Integer empId) {
        // 兜底保护：页码至少为 1，每页最多 100 条，防止前端传超大值拖垮数据库
        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        Page<SalaryVO> page = new Page<>(safeCurrent, safeSize);
        return baseMapper.selectSalaryPage(page, normalizeMonth(salaryMonth), empId);
    }

    @Override
    public SalaryVO getDetail(Integer id) {
        SalaryVO detail = baseMapper.selectDetailById(id);
        if (detail == null) {
            throw new BusinessException(404, "薪资记录不存在");
        }
        return detail;
    }

    @Override
    @Transactional
    public SysSalary createSalary(SalarySaveDTO request) {
        validateEmployee(request.getEmpId());
        SysSalary salary = toEntity(request);
        validateAndFill(salary, null);
        baseMapper.insert(salary);
        return salary;
    }

    @Override
    @Transactional
    public SysSalary updateSalary(Integer id, SalarySaveDTO request) {
        SysSalary exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "薪资记录不存在");
        }
        validateEmployee(request.getEmpId());
        SysSalary salary = toEntity(request);
        salary.setId(id);
        validateAndFill(salary, id);
        baseMapper.updateById(salary);
        return salary;
    }

    @Override
    @Transactional
    public void deleteSalary(Integer id) {
        if (baseMapper.selectById(id) == null) {
            throw new BusinessException(404, "薪资记录不存在");
        }
        baseMapper.deleteById(id);
    }

    /** 校验员工存在；引用无效按参数错误 400 处理，与员工模块“部门不存在”口径一致。 */
    private void validateEmployee(Integer empId) {
        SysEmployee employee = employeeMapper.selectById(empId);
        if (employee == null) {
            throw new BusinessException(400, "所选员工不存在");
        }
    }

    /** 金额兜底、业务校验、同员工同月唯一校验，最后由后端重算实发工资。 */
    private void validateAndFill(SysSalary salary, Integer excludedId) {
        if (salary.getBonus() == null) {
            salary.setBonus(BigDecimal.ZERO);
        }
        if (salary.getDeduction() == null) {
            salary.setDeduction(BigDecimal.ZERO);
        }

        String invalid = SalaryCalculator.validate(
                salary.getBasePay(), salary.getBonus(), salary.getDeduction());
        if (invalid != null) {
            throw new BusinessException(400, invalid);
        }

        Long duplicates = baseMapper.selectCount(Wrappers.<SysSalary>lambdaQuery()
                .eq(SysSalary::getEmpId, salary.getEmpId())
                .eq(SysSalary::getSalaryMonth, salary.getSalaryMonth())
                .ne(excludedId != null, SysSalary::getId, excludedId));
        if (duplicates > 0) {
            throw new BusinessException(400, "该员工 " + salary.getSalaryMonth() + " 月已存在薪资记录");
        }

        salary.setActualPay(SalaryCalculator.actualPay(
                salary.getBasePay(), salary.getBonus(), salary.getDeduction()));
    }

    private SysSalary toEntity(SalarySaveDTO request) {
        SysSalary salary = new SysSalary();
        salary.setEmpId(request.getEmpId());
        salary.setSalaryMonth(request.getSalaryMonth());
        salary.setBasePay(request.getBasePay());
        salary.setBonus(request.getBonus());
        salary.setDeduction(request.getDeduction());
        return salary;
    }

    private static String normalizeMonth(String salaryMonth) {
        return salaryMonth == null || salaryMonth.isBlank() ? null : salaryMonth.strip();
    }
}
