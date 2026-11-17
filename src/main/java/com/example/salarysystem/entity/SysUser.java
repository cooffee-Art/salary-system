package com.example.salarysystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录用户。密码只存 BCrypt 哈希，绝不返回给前端。
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String username;

    @JsonIgnore
    private String password;

    /** ADMIN / HR / EMPLOYEE */
    private String role;

    /** 1=启用，0=停用 */
    private Integer status;

    private LocalDateTime createTime;
}
