package com.example.salarysystem.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.salarysystem.common.BusinessException;
import com.example.salarysystem.entity.SysSalary;
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
public class SysSalaryServiceImpl implements SysSalaryService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final SysSalaryMapper salaryMapper;

    @Override
    public Page<SalaryVO> page(String month, String keyword, long current, long size) {
        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        String safeMonth = normalizeMonth(month, true);
        String safeKeyword = keyword == null ? null : keyword.trim();
        return salaryMapper.selectSalaryPage(new Page<>(safeCurrent, safeSize), safeMonth, safeKeyword);
    }

    @Override
    public List<DeptSalaryReportVO> departmentReport(String month) {
        String safeMonth = normalizeMonth(month, false);
        return salaryMapper.selectDepartmentReport(safeMonth);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalaryGenerateResult generate(String month) {
        String safeMonth = normalizeMonth(month, false);
        YearMonth target = YearMonth.parse(safeMonth);
        if (target.isAfter(YearMonth.now())) {
            throw new BusinessException(400, "不能生成未来月份的工资");
        }
        if (salaryMapper.countByMonth(safeMonth) > 0) {
            throw new BusinessException(400, "该月工资已生成，请勿重复操作");
        }

        List<SalaryBaseRow> bases = salaryMapper.selectPayrollBases(safeMonth);
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
                salary.setActualPay(base);
                salaryMapper.insert(salary);
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

    /**
     * @param allowBlank 查询场景允许不传月份（查全部），报表/生成场景必填
     */
    private String normalizeMonth(String month, boolean allowBlank) {
        if (month == null || month.isBlank()) {
            if (allowBlank) {
                return null;
            }
            throw new BusinessException(400, "月份不能为空，格式为 yyyy-MM");
        }
        String value = month.trim();
        if (!value.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            throw new BusinessException(400, "月份格式必须为 yyyy-MM");
        }
        return value;
    }
}
