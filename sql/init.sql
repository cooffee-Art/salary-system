-- ============================================================
-- 员工薪资管理系统（Java Console Edition）数据库初始化脚本
-- 数据库：salary_db   字符集：utf8mb4
-- 技术栈：原生 JDBC，不依赖任何框架
--
-- 使用方式（命令行）：
--   mysql -uroot -p --default-character-set=utf8mb4 < sql/init.sql
--
-- 使用方式（HeidiSQL / MySQL Workbench）：
--   1. 连接本地 MySQL 8.0；
--   2. 打开本文件；
--   3. 确认当前不是正式库后执行；
--   4. 刷新左侧列表，应看到 4 张表与测试数据。
--
-- 警告：本脚本会 DROP 四张表后重建，仅用于首次初始化或重置测试环境。
--       如果库中已有正式数据，请先备份。
--
-- 表关系：
--   sys_department (1) ──< (N) sys_employee (1) ──< (N) sys_salary
--                          sys_user (emp_id) ──> sys_employee，用于把登录账号
--                                               绑定到具体员工（“我的薪资/我的信息”）
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `salary_db`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `salary_db`;

DROP TABLE IF EXISTS `sys_salary`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `sys_employee`;
DROP TABLE IF EXISTS `sys_department`;

-- ------------------------------------------------------------
-- 1. 部门表
-- ------------------------------------------------------------
CREATE TABLE `sys_department` (
    `id`          INT         NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `dept_name`   VARCHAR(50) NOT NULL COMMENT '部门名称',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dept_name` (`dept_name`)
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
    `gender`    VARCHAR(4)  DEFAULT NULL COMMENT '性别：男/女',
    `email`     VARCHAR(100) NOT NULL COMMENT '邮箱',
    `phone`     VARCHAR(20) NOT NULL COMMENT '手机号',
    `dept_id`   INT         DEFAULT NULL COMMENT '部门ID',
    `hire_date` DATE        DEFAULT NULL COMMENT '入职日期',
    `status`    TINYINT     DEFAULT 1 COMMENT '状态：1在职，0离职',
    `create_time` DATETIME  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_emp_no` (`emp_no`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_emp_name` (`name`),
    KEY `fk_emp_dept` (`dept_id`),
    CONSTRAINT `fk_emp_dept` FOREIGN KEY (`dept_id`) REFERENCES `sys_department` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '员工表';

-- ------------------------------------------------------------
-- 3. 登录用户表
--    password_hash 存 PBKDF2 哈希（pbkdf2$sha256$迭代$盐$哈希），不存明文；
--    emp_id 把账号绑定到员工，普通员工登录后只能看自己的信息与薪资。
-- ------------------------------------------------------------
CREATE TABLE `sys_user` (
    `id`            INT          NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`      VARCHAR(50)  NOT NULL COMMENT '登录名',
    `password_hash` VARCHAR(200) NOT NULL COMMENT 'PBKDF2 密码哈希',
    `role`          ENUM('ADMIN','EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE' COMMENT '角色',
    `status`        TINYINT      DEFAULT 1 COMMENT '1启用，0停用',
    `emp_id`        INT          DEFAULT NULL COMMENT '绑定的员工ID，管理员可为空',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `fk_user_emp` (`emp_id`),
    CONSTRAINT `fk_user_emp` FOREIGN KEY (`emp_id`) REFERENCES `sys_employee` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '登录用户表';

-- ------------------------------------------------------------
-- 4. 薪资记录表
--    uk_emp_month 保证同一员工同一月份只有一条记录，防止重复发薪。
-- ------------------------------------------------------------
CREATE TABLE `sys_salary` (
    `id`           INT           NOT NULL AUTO_INCREMENT COMMENT '薪资记录ID',
    `emp_id`       INT           NOT NULL COMMENT '员工ID',
    `salary_month` CHAR(7)       NOT NULL COMMENT '薪资月份，格式 yyyy-MM',
    `base_pay`     DECIMAL(10,2) NOT NULL COMMENT '基本工资',
    `bonus`        DECIMAL(10,2) DEFAULT 0.00 COMMENT '奖金',
    `deduction`    DECIMAL(10,2) DEFAULT 0.00 COMMENT '扣款',
    `actual_pay`   DECIMAL(10,2) NOT NULL COMMENT '实发工资 = 基本工资 + 奖金 - 扣款',
    `create_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_emp_month` (`emp_id`, `salary_month`),
    KEY `idx_salary_month` (`salary_month`),
    KEY `fk_salary_emp` (`emp_id`),
    CONSTRAINT `fk_salary_emp` FOREIGN KEY (`emp_id`) REFERENCES `sys_employee` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '薪资记录表';

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 5. 测试数据
-- ------------------------------------------------------------
INSERT INTO `sys_department` (`id`, `dept_name`) VALUES
    (1, '技术部'),
    (2, '财务部'),
    (3, '人事部');

INSERT INTO `sys_employee`
    (`id`, `emp_no`, `name`, `gender`, `email`, `phone`, `dept_id`, `hire_date`, `status`)
VALUES
    (1, 'E001', '张三', '男', 'zhangsan@example.com', '13800138000', 1, '2023-01-15', 1),
    (2, 'E002', '李四', '女', 'lisi@example.com',     '13900139000', 2, '2023-03-20', 1),
    (3, 'E003', '王五', '男', 'wangwu@example.com',   '13700137000', 1, '2024-02-01', 1);

-- 默认管理员：admin / Admin@123
-- 测试用普通员工：employee / User@123（绑定员工 2 李四，仅用于角色与权限测试）
INSERT INTO `sys_user` (`username`, `password_hash`, `role`, `status`, `emp_id`) VALUES
    ('admin',
     'pbkdf2$sha256$120000$QurRoGavwlRU2JADgZR8XQ==$vF1IAElSUbvpcdOgnNuXVG2lfXKURd6uurNYqB2nsmU=',
     'ADMIN', 1, NULL),
    ('employee',
     'pbkdf2$sha256$120000$cs/FJ6CMqjkb3D8LhUAwDA==$5X46ZtFB46qwJXWhLms4lmh3U0HmV94LRYOoj2UwZPA=',
     'EMPLOYEE', 1, 2);

INSERT INTO `sys_salary`
    (`emp_id`, `salary_month`, `base_pay`, `bonus`, `deduction`, `actual_pay`)
VALUES
    (1, '2024-09', 8000.00, 1000.00, 500.00, 8500.00),
    (2, '2024-09', 7000.00,  500.00,   0.00, 7500.00),
    (3, '2024-09', 9000.00, 2000.00, 200.00, 10800.00);

-- ------------------------------------------------------------
-- 6. 自检：四张表的记录数
-- ------------------------------------------------------------
SELECT '部门' AS 表名, COUNT(*) AS 记录数 FROM `sys_department`
UNION ALL SELECT '员工', COUNT(*) FROM `sys_employee`
UNION ALL SELECT '用户', COUNT(*) FROM `sys_user`
UNION ALL SELECT '薪资', COUNT(*) FROM `sys_salary`;

-- ------------------------------------------------------------
-- 7. 约束与索引说明（评审要点）
-- ------------------------------------------------------------
-- uk_emp_month(emp_id, salary_month)  同一员工同一月份唯一，防止重复发薪
-- uk_username(sys_user.username)      登录名唯一
-- uk_emp_no / uk_email / uk_phone      工号、邮箱、手机号唯一，对应校验用例 V-08
-- uk_dept_name(sys_department.dept_name) 部门名唯一，避免报表按部门汇总时出现重名
-- idx_salary_month                     报表按月份筛选
-- idx_emp_name                         员工按姓名模糊查询
--
-- 注意：金额统一 DECIMAL(10,2)，禁止用浮点数存钱；后端计算后再写入 actual_pay。
