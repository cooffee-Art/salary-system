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
import com.example.salarysystem.vo.DeptSalaryReportVO;
import com.example.salarysystem.vo.SalaryBaseRow;
import com.example.salarysystem.vo.SalaryGenerateResult;
import com.example.salarysystem.vo.SalaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysSalaryServiceImpl extends ServiceImpl<SysSalaryMapper, SysSalary>
        implements SysSalaryService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final SysEmployeeMapper employeeMapper;

    @Override
    public List<SalaryVO> list(String salaryMonth, Integer empId, String keyword) {
        return baseMapper.selectSalaryList(normalizeMonth(salaryMonth), empId, trimToNull(keyword));
    }

    @Override
    public Page<SalaryVO> pageSalaries(long current, long size, String salaryMonth,
                                       Integer empId, String keyword) {
        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        Page<SalaryVO> page = new Page<>(safeCurrent, safeSize);
        return baseMapper.selectSalaryPage(page, normalizeMonth(salaryMonth), empId, trimToNull(keyword));
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
    @Transactional(rollbackFor = Exception.class)
    public SysSalary createSalary(SalarySaveDTO request) {
        validateEmployee(request.getEmpId());
        SysSalary salary = toEntity(request);
        validateAndFill(salary, null);
        baseMapper.insert(salary);
        return salary;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysSalary updateSalary(Integer id, SalarySaveDTO request) {
        SysSalary existing = baseMapper.selectById(id);
        if (existing == null) {
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
    @Transactional(rollbackFor = Exception.class)
    public void deleteSalary(Integer id) {
        if (baseMapper.selectById(id) == null) {
            throw new BusinessException(404, "薪资记录不存在");
        }
        baseMapper.deleteById(id);
    }

    @Override
    public List<DeptSalaryReportVO> departmentReport(String month) {
        return baseMapper.selectDepartmentReport(requireMonth(month));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalaryGenerateResult generate(String month) {
        String safeMonth = requireMonth(month);
        if (YearMonth.parse(safeMonth).isAfter(YearMonth.now())) {
            throw new BusinessException(400, "不能生成未来月份的工资");
        }
        if (baseMapper.countByMonth(safeMonth) > 0) {
            throw new BusinessException(400, "该月工资已生成，请勿重复操作");
        }

        List<SalaryBaseRow> bases = baseMapper.selectPayrollBases(safeMonth);
        int generated = 0;
        int skipped = 0;
        BigDecimal total = ZERO;

        try {
            for (SalaryBaseRow row : bases) {
                if (row.getBasePay() == null) {
                    skipped++;
                    continue;
                }
                BigDecimal base = row.getBasePay().setScale(2, RoundingMode.HALF_UP);
                SysSalary salary = new SysSalary();
                salary.setEmpId(row.getEmpId());
                salary.setSalaryMonth(safeMonth);
                salary.setBasePay(base);
                salary.setBonus(ZERO);
                salary.setDeduction(ZERO);
                salary.setActualPay(SalaryCalculator.actualPay(base, ZERO, ZERO));
                baseMapper.insert(salary);
                generated++;
                total = total.add(base);
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(400, "该月工资已生成，请勿重复操作");
        }

        if (generated == 0) {
            throw new BusinessException(400, "没有可生成工资的员工，请先确认员工有历史薪资记录");
        }
        return new SalaryGenerateResult(safeMonth, generated, skipped, total.setScale(2, RoundingMode.HALF_UP));
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
            salary.setBonus(ZERO);
        }
        if (salary.getDeduction() == null) {
            salary.setDeduction(ZERO);
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
            throw new BusinessException(400,
                    "该员工 " + salary.getSalaryMonth() + " 月已存在薪资记录");
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

    /** 查询场景允许不传月份；传了就必须是 yyyy-MM。 */
    private String normalizeMonth(String month) {
        if (month == null || month.isBlank()) {
            return null;
        }
        String value = month.trim();
        if (!value.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            throw new BusinessException(400, "月份格式必须为 yyyy-MM");
        }
        return value;
    }

    /** 报表/生成场景月份必填。 */
    private String requireMonth(String month) {
        String value = normalizeMonth(month);
        if (value == null) {
            throw new BusinessException(400, "月份不能为空，格式为 yyyy-MM");
        }
        return value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
