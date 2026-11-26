package com.salary;

import com.salary.model.SalaryRecord;
import com.salary.service.SalaryCalculator;
import com.salary.util.SalarySlipFormatter;

import java.math.BigDecimal;

/**
 * 薪资计算引擎控制台测试入口。
 *
 * <p>覆盖三个场景：</p>
 * <ul>
 *   <li>A：正常计算并打印工资条；</li>
 *   <li>B：精度测试（基本工资 1000.115）；</li>
 *   <li>C：业务异常（扣款大于总工资）。</li>
 * </ul>
 */
public class SalaryTestMain {

    public static void main(String[] args) {
        SalaryCalculator calculator = new SalaryCalculator();

        scenarioA(calculator);
        System.out.println();
        scenarioB(calculator);
        System.out.println();
        scenarioC(calculator);
    }

    /** 场景 A：10000 基本工资 + 2000 奖金 - 1000 扣款 = 11000.00 */
    private static void scenarioA(SalaryCalculator calculator) {
        System.out.println("========== 场景 A：正常计算 ==========");

        SalaryRecord record = new SalaryRecord();
        record.setEmpId("E001");
        record.setEmpName("张三");
        record.setBaseSalary(new BigDecimal("10000"));
        record.setBonus(new BigDecimal("2000"));
        record.setDeduction(new BigDecimal("1000"));

        calculator.calculate(record);
        System.out.println(SalarySlipFormatter.format(record));
    }

    /** 场景 B：1000.115 四舍五入应得到 1000.12 */
    private static void scenarioB(SalaryCalculator calculator) {
        System.out.println("========== 场景 B：精度测试（1000.115） ==========");

        SalaryRecord record = new SalaryRecord();
        record.setEmpId("E002");
        record.setEmpName("李四");
        record.setBaseSalary(new BigDecimal("1000.115"));
        record.setBonus(BigDecimal.ZERO);
        record.setDeduction(BigDecimal.ZERO);

        calculator.calculate(record);
        System.out.println("原始基本工资: " + record.getBaseSalary().toPlainString());
        System.out.println("计算后实发工资: " + record.getActualSalary().toPlainString());
        System.out.println(SalarySlipFormatter.format(record));
    }

    /** 场景 C：扣款 20000 > 总工资 12000，应抛出业务异常 */
    private static void scenarioC(SalaryCalculator calculator) {
        System.out.println("========== 场景 C：业务异常（扣款 20000 > 总工资） ==========");

        SalaryRecord record = new SalaryRecord();
        record.setEmpId("E003");
        record.setEmpName("王五");
        record.setBaseSalary(new BigDecimal("10000"));
        record.setBonus(new BigDecimal("2000"));
        record.setDeduction(new BigDecimal("20000"));

        try {
            calculator.calculate(record);
            System.out.println("未触发异常（不符合预期）");
        } catch (IllegalArgumentException exception) {
            System.out.println("捕获到异常: " + exception.getClass().getSimpleName());
            System.out.println("异常信息: " + exception.getMessage());
        }
    }
}
