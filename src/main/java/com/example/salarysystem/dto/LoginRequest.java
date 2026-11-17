package com.example.salarysystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名最多50个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 100, message = "密码最多100个字符")
    private String password;

    public void setUsername(String username) {
        this.username = username == null ? null : username.strip();
    }
}
