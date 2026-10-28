package com.example.salarysystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.example.salarysystem.entity.SysEmployee;
import com.example.salarysystem.vo.EmployeeVO;

import java.util.List;

/**
 * 员工服务接口。
 * 继承 IService 后，save / updateById / removeById / getById 等标准方法直接可用。
 */
public interface SysEmployeeService extends IService<SysEmployee> {

    /** 查询全部员工（带部门名称） */
    List<EmployeeVO> listAll();

    /** 分页查询，keyword 支持姓名或工号模糊搜索 */
    Page<EmployeeVO> pageEmployees(long current, long size, String keyword);
}
