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
--    规模：6 个部门、13 名员工、4 个登录账号、39 条薪资记录
--    覆盖：6 个部门均有在职员工；2024-07/08/09 连续三个月；含 1 名离职员工
-- ------------------------------------------------------------

-- 5.1 部门
INSERT INTO `sys_department` (`id`, `dept_name`) VALUES
    (1, '技术部'),
    (2, '财务部'),
    (3, '人事部'),
    (4, '市场部'),
    (5, '行政部'),
    (6, '运维部');

-- 5.2 员工
INSERT INTO `sys_employee`
    (`id`, `emp_no`, `name`, `gender`, `email`, `phone`, `dept_id`, `hire_date`, `status`)
VALUES
    (1,  'E001', '张伟', '男', 'zhangwei@example.com',  '13800138001', 1, '2021-03-15', 1),
    (2,  'E002', '李娜', '女', 'lina@example.com',      '13900139002', 2, '2020-07-01', 1),
    (3,  'E003', '王强', '男', 'wangqiang@example.com', '13700137003', 1, '2022-05-09', 1),
    (4,  'E004', '刘洋', '女', 'liuyang@example.com',   '13600136004', 3, '2021-11-20', 1),
    (5,  'E005', '陈静', '女', 'chenjing@example.com',  '13500135005', 4, '2023-02-13', 1),
    (6,  'E006', '杨帆', '男', 'yangfan@example.com',   '13400134006', 5, '2019-09-02', 1),
    (7,  'E007', '赵敏', '女', 'zhaomin@example.com',   '13300133007', 2, '2022-08-16', 1),
    (8,  'E008', '孙浩', '男', 'sunhao@example.com',    '13200132008', 1, '2023-06-01', 1),
    (9,  'E009', '周婷', '女', 'zhouting@example.com',  '13100131009', 3, '2024-01-08', 1),
    (10, 'E010', '吴磊', '男', 'wulei@example.com',     '13000130010', 4, '2021-04-26', 1),
    (11, 'E011', '徐丽', '女', 'xuli@example.com',      '18900189011', 5, '2022-12-05', 1),
    (12, 'E012', '马超', '男', 'machao@example.com',    '18800188012', 6, '2020-10-19', 0),
    (13, 'E013', '韩雪', '女', 'hanxue@example.com',    '18700187013', 6, '2023-09-11', 1);

-- 5.3 登录账号
--     管理员：        admin / Admin@123        （ADMIN，不绑定员工）
--     演示员工：      employee / User@123       （EMPLOYEE，绑定员工 2 李娜）
--     普通员工示例：  zhangwei / Employee@1     （EMPLOYEE，绑定员工 1 张伟）
--     离职员工示例：  machao / Employee@1       （EMPLOYEE，绑定员工 12 马超，员工已离职）
INSERT INTO `sys_user` (`username`, `password_hash`, `role`, `status`, `emp_id`) VALUES
    ('admin',
     'pbkdf2$sha256$120000$QurRoGavwlRU2JADgZR8XQ==$vF1IAElSUbvpcdOgnNuXVG2lfXKURd6uurNYqB2nsmU=',
     'ADMIN', 1, NULL),
    ('employee',
     'pbkdf2$sha256$120000$cs/FJ6CMqjkb3D8LhUAwDA==$5X46ZtFB46qwJXWhLms4lmh3U0HmV94LRYOoj2UwZPA=',
     'EMPLOYEE', 1, 2),
    ('zhangwei',
     'pbkdf2$sha256$120000$0krKE5xBnawrRbNNc9oKaw==$iqGBoeItTT1XeYRNw4/eHfCvCfAd5R4reavZLyvi/pE=',
     'EMPLOYEE', 1, 1),
    ('machao',
     'pbkdf2$sha256$120000$S7DuMm/S/C+ZogsO/Prl/Q==$nwsKKkNZt0JOxSxu0cQQpqyuLHs+48iJK2E937WDq90=',
     'EMPLOYEE', 1, 12);

-- 5.4 薪资记录：2024-07 / 08 / 09 三个月，12 名员工各 3 条
INSERT INTO `sys_salary`
    (`emp_id`, `salary_month`, `base_pay`, `bonus`, `deduction`, `actual_pay`)
VALUES
    -- 2024-07
    (1,  '2024-07', 8500.00,  800.00, 200.00,  9100.00),
    (2,  '2024-07', 7800.00,  600.00,   0.00,  8400.00),
    (3,  '2024-07', 9000.00, 1000.00, 300.00,  9700.00),
    (4,  '2024-07', 7200.00,  400.00,   0.00,  7600.00),
    (5,  '2024-07', 7500.00,  700.00, 200.00,  8000.00),
    (6,  '2024-07', 6800.00,  300.00,   0.00,  7100.00),
    (7,  '2024-07', 7600.00,  550.00, 180.00,  7970.00),
    (8,  '2024-07', 8200.00,  800.00, 250.00,  8750.00),
    (9,  '2024-07', 7000.00,  380.00, 120.00,  7260.00),
    (10, '2024-07', 7300.00,  650.00, 150.00,  7800.00),
    (11, '2024-07', 6900.00,  320.00,   0.00,  7220.00),
    (12, '2024-07', 8000.00,  750.00, 300.00,  8450.00),
    (13, '2024-07', 7800.00,  600.00, 200.00,  8200.00),
    -- 2024-08
    (1,  '2024-08', 8500.00,  600.00, 350.00,  8750.00),
    (2,  '2024-08', 7800.00,  500.00, 200.00,  8100.00),
    (3,  '2024-08', 9000.00,  900.00,   0.00,  9900.00),
    (4,  '2024-08', 7200.00,  350.00, 100.00,  7450.00),
    (5,  '2024-08', 7500.00,  650.00, 150.00,  8000.00),
    (6,  '2024-08', 6800.00,  300.00,   0.00,  7100.00),
    (7,  '2024-08', 7600.00,  480.00,   0.00,  8080.00),
    (8,  '2024-08', 8200.00,  750.00, 300.00,  8650.00),
    (9,  '2024-08', 7000.00,  400.00,   0.00,  7400.00),
    (10, '2024-08', 7300.00,  600.00, 220.00,  7680.00),
    (11, '2024-08', 6900.00,  350.00,  90.00,  7160.00),
    (12, '2024-08', 8000.00,  700.00,   0.00,  8700.00),
    (13, '2024-08', 7800.00,  550.00,   0.00,  8350.00),
    -- 2024-09
    (1,  '2024-09', 8500.00, 1000.00, 200.00,  9300.00),
    (2,  '2024-09', 7800.00,  700.00, 150.00,  8350.00),
    (3,  '2024-09', 9000.00, 1200.00, 400.00,  9800.00),
    (4,  '2024-09', 7200.00,  500.00, 250.00,  7450.00),
    (5,  '2024-09', 7500.00,  900.00, 300.00,  8100.00),
    (6,  '2024-09', 6800.00,  400.00, 120.00,  7080.00),
    (7,  '2024-09', 7600.00,  620.00, 220.00,  8000.00),
    (8,  '2024-09', 8200.00, 1100.00, 350.00,  8950.00),
    (9,  '2024-09', 7000.00,  450.00, 200.00,  7250.00),
    (10, '2024-09', 7300.00,  850.00, 180.00,  7970.00),
    (11, '2024-09', 6900.00,  420.00, 150.00,  7170.00),
    (12, '2024-09', 8000.00,  900.00, 260.00,  8640.00),
    (13, '2024-09', 7800.00,  700.00, 180.00,  8320.00);

-- ------------------------------------------------------------
-- 6. 自检：记录数 + 金额一致性
-- ------------------------------------------------------------
SELECT '部门' AS 表名, COUNT(*) AS 记录数 FROM `sys_department`
UNION ALL SELECT '员工', COUNT(*) FROM `sys_employee`
UNION ALL SELECT '用户', COUNT(*) FROM `sys_user`
UNION ALL SELECT '薪资', COUNT(*) FROM `sys_salary`;

-- 期望 0：实发工资必须等于“基本工资 + 奖金 - 扣款”，防止录入算错
SELECT '实发金额异常行数' AS 检查项,
       COUNT(*) AS 异常数
FROM `sys_salary`
WHERE `actual_pay` <> `base_pay` + `bonus` - `deduction`;

-- 期望 0：扣款不能超过“基本工资 + 奖金”（对应校验用例 V-10）
SELECT '扣款超额行数' AS 检查项,
       COUNT(*) AS 异常数
FROM `sys_salary`
WHERE `deduction` > `base_pay` + `bonus`;

-- 各部门在职人数与月度薪资合计，便于核对报表口径
SELECT d.`dept_name`                       AS 部门,
       COUNT(DISTINCT e.`id`)              AS 在职人数,
       COALESCE(SUM(s.`actual_pay`), 0.00) AS 九月实发合计
FROM `sys_department` d
LEFT JOIN `sys_employee` e ON e.`dept_id` = d.`id` AND e.`status` = 1
LEFT JOIN `sys_salary` s ON s.`emp_id` = e.`id` AND s.`salary_month` = '2024-09'
GROUP BY d.`id`, d.`dept_name`
ORDER BY d.`id`;

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
