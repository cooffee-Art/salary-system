-- ============================================================
-- 员工薪资管理系统 数据库初始化脚本
-- 数据库：salary_db
-- 字符集：utf8mb4
--
-- ⚠️ 警告：本脚本会 DROP 三张表后重建，仅供首次初始化或重置测试环境。
--    如果库里已有正式数据，请先备份，不要直接执行！
--
-- 使用方式（HeidiSQL）：
--   1. 连接到本地 MySQL 8.0；
--   2. 打开本文件；
--   3. 确认当前库不是生产库后，点击“执行”；
--   4. 刷新左侧列表，应看到 3 张表和测试数据。
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `salary_db`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `salary_db`;

DROP TABLE IF EXISTS `sys_salary`;
DROP TABLE IF EXISTS `sys_employee`;
DROP TABLE IF EXISTS `sys_department`;

-- ------------------------------------------------------------
-- 1. 部门表
-- ------------------------------------------------------------
CREATE TABLE `sys_department` (
    `id`          INT         NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `dept_name`   VARCHAR(50) NOT NULL COMMENT '部门名称',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '部门表';

-- ------------------------------------------------------------
-- 2. 员工表
-- ------------------------------------------------------------
CREATE TABLE `sys_employee` (
    `id`        INT         NOT NULL AUTO_INCREMENT COMMENT '员工ID',
    `emp_no`    VARCHAR(20) NOT NULL COMMENT '工号',
    `name`      VARCHAR(50) NOT NULL COMMENT '姓名',
    `gender`    CHAR(1)     DEFAULT NULL COMMENT '性别(男/女)',
    `phone`     VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `dept_id`   INT         DEFAULT NULL COMMENT '部门ID',
    `hire_date` DATE        DEFAULT NULL COMMENT '入职日期',
    `status`    TINYINT     DEFAULT 1 COMMENT '状态(1在职,0离职)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `emp_no` (`emp_no`),
    KEY `fk_emp_dept` (`dept_id`),
    CONSTRAINT `fk_emp_dept` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '员工表';

-- ------------------------------------------------------------
-- 3. 薪资记录表
-- ------------------------------------------------------------
CREATE TABLE `sys_salary` (
    `id`           INT           NOT NULL AUTO_INCREMENT COMMENT '薪资记录ID',
    `emp_id`       INT           NOT NULL COMMENT '员工ID',
    `salary_month` VARCHAR(7)    NOT NULL COMMENT '薪资月份(如2024-09)',
    `base_pay`     DECIMAL(10,2) NOT NULL COMMENT '基本工资',
    `bonus`        DECIMAL(10,2) DEFAULT 0.00 COMMENT '奖金',
    `deduction`    DECIMAL(10,2) DEFAULT 0.00 COMMENT '扣款',
    `actual_pay`   DECIMAL(10,2) NOT NULL COMMENT '实发工资',
    `create_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `fk_salary_emp` (`emp_id`),
    CONSTRAINT `fk_salary_emp` FOREIGN KEY (`emp_id`) REFERENCES `sys_employee` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '薪资记录表';

-- ------------------------------------------------------------
-- 4. 测试数据
-- ------------------------------------------------------------
INSERT INTO `sys_department` (`id`, `dept_name`) VALUES
    (1, '技术部'),
    (2, '财务部'),
    (3, '人事部');

INSERT INTO `sys_employee` (`id`, `emp_no`, `name`, `gender`, `phone`, `dept_id`, `hire_date`, `status`) VALUES
    (1, 'E001', '张三', '男', '13800138000', 1, '2023-01-15', 1),
    (2, 'E002', '李四', '女', '13900139000', 2, '2023-03-20', 1),
    (3, 'E003', '王五', '男', '13700137000', 1, '2024-02-01', 1);

INSERT INTO `sys_salary` (`emp_id`, `salary_month`, `base_pay`, `bonus`, `deduction`, `actual_pay`) VALUES
    (1, '2024-09', 8000.00, 1000.00, 500.00, 8500.00),
    (2, '2024-09', 7000.00, 500.00, 0.00, 7500.00),
    (3, '2024-09', 9000.00, 2000.00, 200.00, 10800.00);

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 5. 自检
-- ------------------------------------------------------------
SELECT '部门' AS 表名, COUNT(*) AS 记录数 FROM `sys_department`
UNION ALL
SELECT '员工', COUNT(*) FROM `sys_employee`
UNION ALL
SELECT '薪资', COUNT(*) FROM `sys_salary`;

-- ------------------------------------------------------------
-- 6. 建议的后续优化（先别急着执行，交给组员 C 评估）
-- ------------------------------------------------------------
-- 同一员工、同一月份只允许一条薪资记录，防止重复发薪：
-- ALTER TABLE `sys_salary`
--     ADD UNIQUE KEY `uk_emp_month` (`emp_id`, `salary_month`);
--
-- 员工姓氏搜索、部门名称搜索的索引（数据量大时再加）：
-- ALTER TABLE `sys_employee` ADD INDEX `idx_emp_name` (`name`);
-- ALTER TABLE `sys_department` ADD INDEX `idx_dept_name` (`dept_name`);
