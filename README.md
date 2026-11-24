# 员工薪资管理系统（Java Console Edition）

> 大学期末项目 · 纯 Java 控制台应用程序
>
> 技术栈：Java SE + JDBC + MySQL
> **不使用 Spring Boot、Vue、浏览器或任何 Web 容器。**
>
> **Branch status:** `feature/console-refactor` — console skeleton, bilingual i18n and login stub are ready; employee/salary/DB modules are still in progress.
>
> **分支状态：** 控制台骨架、双语语言包、登录占位已完成；员工、薪资、数据库模块由 Ming / Leo / Lucas 继续实现。

---

## 1. 项目简介

本项目是一个面向学校/企业场景的员工薪资管理控制台程序，支持：

- 管理员 / 普通员工登录与角色权限
- 员工信息管理（新增、查询、修改、删除）
- 部门管理
- 薪资计算、工资条打印、月度报表
- 中英文双语切换
- 严格的数据验证（邮箱、手机号、密码、金额等）
- MySQL 数据持久化（原生 JDBC）

程序通过终端菜单交互，不依赖图形界面或浏览器。

---

## 2. 技术栈

| 层次 | 技术 | 说明 |
| --- | --- | --- |
| 语言 | Java SE 17+ | 纯 Java，无 Spring |
| 数据库 | MySQL 8.0+ | InnoDB / utf8mb4 |
| 持久化 | 原生 JDBC | `java.sql` + MySQL Connector/J |
| 本地化 | `ResourceBundle` | `messages_zh.properties` / `messages_en.properties` |
| 输入 | `Scanner` | 控制台交互 |
| 构建 | `javac` / `java` | 不依赖 Maven/Gradle |

> 项目中不应出现 `spring-boot`、`spring-web`、`vue` 等依赖。
>
> 评审时可执行 `grep -ri "spring" src/` 检查，结果应为空。

---

## 3. 运行环境要求

| 软件 | 版本要求 |
| --- | --- |
| JDK | 17 或 21（推荐 21 LTS） |
| MySQL | 8.0+ |
| MySQL Connector/J | 8.4.0（放在 `lib/`） |
| 终端 | Windows PowerShell / macOS Terminal / Linux Shell |
| IDE（可选） | IntelliJ IDEA / VS Code |

### 检查环境

```bash
java -version
javac -version
mysql --version
```

确保 `java` 和 `javac` 来自同一个 JDK。

---

## 4. 项目结构

```text
salary-system/
├── src/
│   └── com/salary/console/
│       ├── Main.java                 # 程序入口
│       ├── config/
│       │   └── DatabaseConfig.java   # JDBC URL / 用户名 / 密码
│       ├── db/
│       │   └── DatabaseManager.java  # 连接管理
│       ├── i18n/
│       │   └── I18n.java             # ResourceBundle 封装
│       ├── model/                    # User / Employee / Salary 等
│       ├── dao/                      # JDBC DAO
│       ├── service/                  # 业务逻辑
│       ├── validation/
│       │   └── Validator.java        # 邮箱、手机号、密码校验
│       └── ui/
│           └── ConsoleMenu.java      # 主菜单与输入循环
├── resources/
│   ├── messages_zh.properties        # 中文语言包
│   └── messages_en.properties        # 英文语言包
├── sql/
│   └── init.sql                      # 建库、建表、测试数据
├── lib/
│   └── mysql-connector-j-8.4.0.jar   # JDBC 驱动
├── out/                              # 编译输出（git 忽略）
└── README.md
```

> `src/.../Main.java` 控制台骨架已提交；`sql/init.sql` 由 Lucas 提供，员工/薪资模块由 Ming / Leo 继续实现。

---

## 5. 初始化数据库

### 5.1 启动 MySQL

确保 MySQL 服务已启动：

```bash
# Windows（管理员 PowerShell）
net start MySQL80

# macOS（Homebrew）
brew services start mysql
```

### 5.2 执行初始化脚本

在项目根目录执行：

```bash
mysql -uroot -p --default-character-set=utf8mb4 < sql/init.sql
```

也可以使用 MySQL Workbench 或 HeidiSQL：

1. 连接本地 MySQL；
2. 打开 `sql/init.sql`；
3. 选择目标数据库；
4. 执行脚本。

脚本会创建：

```text
salary_db
├── sys_user          # 登录用户与角色
├── sys_department    # 部门
├── sys_employee      # 员工
└── sys_salary        # 月度薪资
```

> 警告：`init.sql` 会删除并重建这四张表，仅用于本地练习或测试库。
>
> 不要直接对包含正式数据的数据库执行。

### 5.3 默认账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `Admin@123` |

> 控制台骨架还内置 `employee / User@123` 用于测试普通员工菜单；接入 JDBC 登录后以 `sys_user` 表为准。
>
> 普通员工账号由管理员在系统内创建。
>
> 首次登录后建议修改默认密码。

---

## 6. 配置数据库连接

打开：

```text
src/com/salary/console/config/DatabaseConfig.java
```

修改以下常量：

```java
public final class DatabaseConfig {
    public static final String URL =
            "jdbc:mysql://localhost:3306/salary_db"
            + "?useUnicode=true&characterEncoding=UTF-8"
            + "&serverTimezone=Asia/Shanghai"
            + "&useSSL=false&allowPublicKeyRetrieval=true";

    public static final String USER = "root";
    public static final String PASSWORD = "your_mysql_password";
}
```

说明：

- 数据库名默认 `salary_db`，与 `sql/init.sql` 一致。
- 如果 MySQL 端口不是 `3306`，修改 URL。
- 不要把真实密码提交到公开仓库。

---

## 7. 编译

### Windows PowerShell

```powershell
cd C:\path\to\salary-system

New-Item -ItemType Directory -Force out | Out-Null

javac -encoding UTF-8 -cp "lib/*" -d out `
  (Get-ChildItem -Recurse -Filter *.java -Path src | ForEach-Object { $_.FullName })
```

### macOS / Linux

```bash
cd /path/to/salary-system

mkdir -p out

javac -encoding UTF-8 -cp "lib/*" -d out $(find src -name "*.java")
```

编译成功后，`out/` 下会出现 `com/salary/console/...` 的 `.class` 文件。

### 在 IntelliJ IDEA 中编译（可选）

1. `File` → `Open`，选择项目根目录；
2. 将 `src` 标记为 Sources Root；
3. 将 `resources` 标记为 Resources Root；
4. 将 `lib/mysql-connector-j-8.4.0.jar` 加入 Module Dependencies；
5. 运行 `com.salary.console.Main`。

---

## 8. 运行

### Windows PowerShell

```powershell
chcp 65001

java "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" `
  -cp "out;resources;lib/*" com.salary.console.Main
```

### macOS / Linux

```bash
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 \
  -cp "out:resources:lib/*" com.salary.console.Main
```

说明：

- Windows 的 classpath 分隔符是 `;`，macOS/Linux 是 `:`。
- `resources` 必须在 classpath 中，否则 `ResourceBundle` 找不到语言包。
- `lib/*` 用于加载 MySQL JDBC 驱动。

---

## 9. 控制台菜单

### 启动后

```text
========================================
 Employee Salary Management System
 员工薪资管理系统
========================================
 1. English
 2. 中文
 0. Exit / 退出
========================================
Please select / 请选择:
```

### 登录后（管理员）

```text
1. Employee Management / 员工管理
2. Salary Management   / 薪资管理
3. Department Report   / 部门报表
4. Switch Language     / 切换语言
5. Logout              / 退出登录
0. Exit                / 退出系统
```

### 登录后（普通员工）

```text
1. My Profile          / 我的信息
2. My Salary           / 我的薪资
3. Switch Language     / 切换语言
4. Logout              / 退出登录
0. Exit                / 退出系统
```

---

## 10. 测试中英文切换

### 测试步骤

1. 启动程序，在语言选择界面输入 `2`，进入中文界面。
2. 使用 `admin / Admin@123` 登录。
3. 进入主菜单，确认所有菜单项、提示、错误信息均为中文。
4. 选择 `4. Switch Language / 切换语言`，输入 `1` 切换英文。
5. 确认主菜单、输入提示、成功/失败提示全部变为英文。
6. 在员工新增页面输入非法邮箱 `bad-email`，确认英文错误提示。
7. 再切回中文，重复输入 `bad-email`，确认中文错误提示。
8. 退出程序并重新启动，确认语言选择界面正常出现。

### 验收标准

- [ ] 启动时可选择 English / 中文
- [ ] 菜单、提示、表头、验证错误均随语言切换
- [ ] 切换语言不需要重启程序
- [ ] 两种语言的输出均无乱码
- [ ] 语言包缺失时程序不崩溃，回退到默认语言

### 语言包文件

```text
resources/messages_zh.properties
resources/messages_en.properties
```

示例：

```properties
# messages_zh.properties
menu.employee=员工管理
menu.salary=薪资管理
validation.email.invalid=邮箱格式不正确
validation.phone.invalid=手机号必须是 11 位，且以 1 开头
validation.password.weak=密码至少 8 位，且包含大小写字母、数字和特殊字符
```

```properties
# messages_en.properties
menu.employee=Employee Management
menu.salary=Salary Management
validation.email.invalid=Invalid email format
validation.phone.invalid=Phone number must be 11 digits and start with 1
validation.password.weak=Password must be at least 8 characters and include upper/lowercase letters, a digit and a special character
```

### 在 Java 中使用语言包

```java
import com.salary.console.i18n.I18n;
import java.util.Locale;

// 读取普通文案
System.out.println(I18n.get("menu.employee"));

// 带占位符的文案
System.out.println(I18n.get("validation.required", "Email"));

// 运行时切换
I18n.setLocale(Locale.ENGLISH);
```

约定：

- 新增任何面向用户的文本，必须同时补 `messages_zh.properties` 和 `messages_en.properties`。
- Java 代码中不要硬编码中文/英文提示。
- key 使用小写点号命名，例如 `employee.name.required`、`salary.generate.success`。
- 带参数的文案统一用 `{0}`、`{1}` 占位。

---

## 11. 测试数据验证

### 验证规则

| 字段 | 规则 |
| --- | --- |
| 邮箱 | 必须符合 `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$` |
| 手机号 | 必须符合 `^1[3-9]\d{9}$`，共 11 位 |
| 密码 | 至少 8 位，包含大写、小写、数字、特殊字符 |
| 用户名 | 4–20 位字母、数字或下划线，唯一 |
| 基本工资 | 非负，最多两位小数 |
| 奖金 / 扣款 | 非负，扣款不能大于基本工资 + 奖金 |
| 必填项 | 姓名、部门、邮箱、手机号不能为空 |

### 测试用例

| 编号 | 输入 | 预期结果 |
| --- | --- | --- |
| V-01 | 邮箱 `zhangsan@example.com` | 通过 |
| V-02 | 邮箱 `zhangsan@` | 失败：邮箱格式不正确 |
| V-03 | 手机号 `13800138000` | 通过 |
| V-04 | 手机号 `1380013800` | 失败：手机号必须是 11 位 |
| V-05 | 密码 `Admin@123` | 通过 |
| V-06 | 密码 `123456` | 失败：密码强度不足 |
| V-07 | 密码 `abcdefgh` | 失败：缺少大写、数字、特殊字符 |
| V-08 | 重复用户名 / 邮箱 / 手机号 | 失败：数据已存在 |
| V-09 | 基本工资 `-100` | 失败：金额不能为负 |
| V-10 | 扣款大于基本工资 + 奖金 | 失败：扣款超额 |
| V-11 | 姓名为空 | 失败：姓名不能为空 |

### 黑白盒测试建议

- 黑盒：只通过控制台输入，验证提示和数据库结果。
- 白盒：直接调用 `Validator` 方法，验证正则和边界条件。
- 每次验证分别在中文、英文下各执行一次，确认错误信息已本地化。

---

## 12. 常见问题

### 12.1 `Access denied for user 'root'@'localhost'`

检查 `DatabaseConfig.PASSWORD` 是否与 MySQL 密码一致。

### 12.2 `Unknown database 'salary_db'`

先执行：

```bash
mysql -uroot -p < sql/init.sql
```

### 12.3 `No suitable driver found for jdbc:mysql://...`

确认 `lib/mysql-connector-j-8.4.0.jar` 存在，并且运行时 classpath 包含 `lib/*`。

### 12.4 中文乱码

- 编译时加 `-encoding UTF-8`；
- 运行时加 `-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8`；
- Windows 终端执行 `chcp 65001`；
- 数据库、表、连接字符集统一使用 `utf8mb4`。

### 12.5 `ResourceBundle` 找不到语言包

确认 `resources` 在 classpath 中，且文件名严格为：

```text
messages_zh.properties
messages_en.properties
```

### 12.6 找不到 `Main` 类

确认编译输出在 `out/`，运行命令中的主类是：

```text
com.salary.console.Main
```

---

## 13. Git 分支管理建议

当前 `main` 仍保留可运行的 Web 版本；控制台重构在独立分支完成，避免重构期间破坏主分支。

### 13.1 分支模型

```text
main
└── feature/console-refactor          # 长期集成分支
    ├── feature/console-refactor/architecture   # Charles：架构 + 双语
    ├── feature/console-refactor/employee       # Ming：员工 + 验证
    ├── feature/console-refactor/salary         # Leo：薪资 + 报表
    └── feature/console-refactor/db-auth        # Lucas：数据库 + 登录 + 测试
```

| 分支 | 用途 | 合并目标 |
| --- | --- | --- |
| `main` | 稳定可演示版本 | 只接受验收通过的合并 |
| `feature/console-refactor` | 控制台重构集成 | 验收后合并到 `main` |
| `feature/console-refactor/*` | 个人短期分支 | PR 到 `feature/console-refactor` |
| `hotfix/*` | 紧急修复 | `main` + `feature/console-refactor` |
| `docs/*` | 文档修改 | 对应分支 |

### 13.2 初始化分支

```bash
git switch main
git pull origin main
git switch -c feature/console-refactor
git push -u origin feature/console-refactor
```

### 13.3 成员开发流程

```bash
# 每次开发前同步集成分支
git switch feature/console-refactor
git pull origin feature/console-refactor

# 创建自己的短期分支
git switch -c feature/console-refactor/employee

# 开发、编译、自测
git add .
git commit -m "feat(console-employee): 实现员工新增与数据验证"
git push -u origin feature/console-refactor/employee
```

然后在 GitHub / Gitee 发起 PR，目标分支选择：

```text
feature/console-refactor
```

不要直接 PR 到 `main`。

### 13.4 合并与验收

1. 每个 PR 至少 1 人 review（建议 Charles）。
2. 合并前必须满足：
   - `javac` 编译通过；
   - 程序可启动；
   - 中英文各跑一遍；
   - 数据验证用例通过；
   - `sql/init.sql` 可在空库执行；
   - 没有引入 Spring / Vue 依赖。
3. 每周至少一次将 `feature/console-refactor` 的最新代码合并回个人分支，减少冲突。
4. 控制台版本通过老师验收后，再将 `feature/console-refactor` 合并到 `main`，并打标签：

```bash
git switch main
git merge --no-ff feature/console-refactor
git tag -a v1.0-console -m "Console version accepted"
git push origin main --tags
```

### 13.5 提交信息规范

```text
feat(console): 新增控制台主菜单
feat(i18n): 添加中英文语言包
feat(validation): 实现邮箱/手机号/密码校验
feat(salary): 实现工资计算与工资条
fix(console): 修复语言切换后提示未刷新
test(validation): 补充边界测试用例
docs: 更新 README 与数据库脚本
```

### 13.6 注意事项

- 不要直接在 `main` 上提交重构代码。
- 不要强制推送到共享分支。
- 合并前先 `git pull --rebase origin feature/console-refactor`。
- 数据库脚本变更必须同步通知全组。
- 重构期间保持 `main` 可运行，便于随时演示 Web 版本。

---

## 14. 团队成员与分工（Console Version）

| 成员角色 | 姓名 | 核心模块 | 具体职责 |
| :--- | :--- | :--- | :--- |
| **组长（架构/本地化）** | Charles | 系统架构<br>双语本地化 | 1. 设计整体控制台程序架构与主菜单循环。<br>2. 实现中英文语言包（`ResourceBundle`），处理全局语言切换。<br>3. 负责 Git 版本控制、代码合并与最终审查。 |
| **后端开发（核心业务）** | Ming | 员工管理<br>数据验证 | 1. 编写员工与部门控制台交互逻辑（`Scanner` 输入）。<br>2. 实现数据验证类（正则校验邮箱、手机号、密码复杂度）。<br>3. 使用原生 JDBC 将合法数据持久化到 MySQL。 |
| **业务逻辑（计算引擎）** | Leo | 薪资计算<br>报表格式化 | 1. 编写薪资计算引擎（基本工资、考勤扣款、奖金等）。<br>2. 使用 `String.format` 输出可读性高的工资条。<br>3. 处理空指针、计算溢出等业务异常。 |
| **数据库与测试（DBA/QA）** | Lucas | 数据库设计<br>权限与测试 | 1. 设计 MySQL 表结构，输出一键初始化脚本 `sql/init.sql`。<br>2. 编写控制台登录与角色权限拦截（管理员/普通员工）。<br>3. 执行黑盒测试，验证数据校验与双语切换。 |

---

## 15. 验收清单

- [ ] 不依赖 Spring Boot / Vue
- [ ] 使用原生 JDBC 连接 MySQL
- [ ] `sql/init.sql` 可在空库执行
- [ ] 默认管理员可登录
- [ ] 中英文可切换，菜单和错误提示均本地化
- [ ] 邮箱、手机号、密码校验生效
- [ ] 员工 CRUD 正常
- [ ] 薪资计算与工资条正常
- [ ] 管理员/普通员工权限区分
- [ ] `README.md` 中的命令可直接运行
