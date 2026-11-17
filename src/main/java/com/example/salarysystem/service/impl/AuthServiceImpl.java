package com.example.salarysystem.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.salarysystem.common.BusinessException;
import com.example.salarysystem.dto.LoginRequest;
import com.example.salarysystem.entity.SysUser;
import com.example.salarysystem.mapper.SysUserMapper;
import com.example.salarysystem.security.AuthUser;
import com.example.salarysystem.security.JwtTokenProvider;
import com.example.salarysystem.service.AuthService;
import com.example.salarysystem.vo.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已停用，请联系管理员");
        }

        String token = tokenProvider.createToken(user.getUsername(), user.getRole());
        return new LoginResponse(
                token,
                "Bearer",
                tokenProvider.getExpirationSeconds(),
                user.getUsername(),
                user.getRole()
        );
    }

    @Override
    public AuthUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser user)) {
            throw new BusinessException(401, "未登录");
        }
        return user;
    }
}
