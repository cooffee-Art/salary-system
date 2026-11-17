package com.example.salarysystem.service;

import com.example.salarysystem.dto.LoginRequest;
import com.example.salarysystem.security.AuthUser;
import com.example.salarysystem.vo.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    AuthUser currentUser();
}
