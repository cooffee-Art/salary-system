DROP TABLE IF EXISTS sys_salary;
DROP TABLE IF EXISTS sys_employee;
DROP TABLE IF EXISTS sys_department;

CREATE TABLE sys_department (
    id INT AUTO_INCREMENT PRIMARY KEY,
    dept_name VARCHAR(50) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE sys_employee (
    id INT AUTO_INCREMENT PRIMARY KEY,
    emp_no VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    gender CHAR(1), phone VARCHAR(20), dept_id INT,
    hire_date DATE, status TINYINT DEFAULT 1,
    CONSTRAINT fk_emp_dept FOREIGN KEY (dept_id) REFERENCES sys_department(id)
);
CREATE TABLE sys_salary (
    id INT AUTO_INCREMENT PRIMARY KEY, emp_id INT NOT NULL,
    salary_month VARCHAR(7) NOT NULL, base_pay DECIMAL(10,2) NOT NULL,
    bonus DECIMAL(10,2) DEFAULT 0, deduction DECIMAL(10,2) DEFAULT 0,
    actual_pay DECIMAL(10,2) NOT NULL, create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_salary_emp FOREIGN KEY (emp_id) REFERENCES sys_employee(id)
);
INSERT INTO sys_department(id, dept_name) VALUES (1, '技术部'), (2, '财务部');
INSERT INTO sys_employee(id, emp_no, name, dept_id, phone, hire_date, status) VALUES
    (1, 'E001', '张三', 1, '13800138000', '2023-01-15', 1),
    (2, 'E002', '李四', 2, NULL, NULL, 1),
    (3, 'E003', '张五', NULL, NULL, NULL, 0);
INSERT INTO sys_salary(emp_id, salary_month, base_pay, actual_pay)
VALUES (1, '2024-09', 8000, 8000);
