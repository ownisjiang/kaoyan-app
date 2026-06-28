package com.yantiku.module.auth.controller;

import com.yantiku.common.dto.ApiResponse;
import com.yantiku.module.auth.model.dto.LoginDTO;
import com.yantiku.module.auth.model.dto.RefreshTokenDTO;
import com.yantiku.module.auth.model.dto.RegisterDTO;
import com.yantiku.module.auth.model.vo.AuthVO;
import com.yantiku.module.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 *
 * @author yantiku
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     *
     * @param dto 注册信息
     * @return 认证信息
     */
    @PostMapping("/register")
    public ApiResponse<AuthVO> register(@Validated @RequestBody RegisterDTO dto) {
        log.info("【注册】手机号: {}", dto.getPhone());
        AuthVO authVO = authService.register(dto);
        return ApiResponse.ok(authVO);
    }

    /**
     * 用户登录
     *
     * @param dto 登录信息
     * @return 认证信息
     */
    @PostMapping("/login")
    public ApiResponse<AuthVO> login(@Validated @RequestBody LoginDTO dto) {
        log.info("【登录】手机号: {}", dto.getPhone());
        AuthVO authVO = authService.login(dto);
        return ApiResponse.ok(authVO);
    }

    /**
     * 刷新令牌
     *
     * @param dto 刷新令牌信息
     * @return 新的认证信息
     */
    @PostMapping("/refresh")
    public ApiResponse<AuthVO> refresh(@Validated @RequestBody RefreshTokenDTO dto) {
        log.info("【刷新令牌】");
        AuthVO authVO = authService.refreshToken(dto.getRefreshToken());
        return ApiResponse.ok(authVO);
    }

    /**
     * 退出登录
     *
     * @param refreshToken 刷新令牌
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Refresh-Token") String refreshToken) {
        log.info("【退出登录】");
        authService.logout(refreshToken);
        return ApiResponse.ok();
    }

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @return 成功响应
     */
    @PostMapping("/send-sms-code")
    public ApiResponse<Void> sendSmsCode(@RequestParam String phone) {
        log.info("【发送短信验证码】手机号: {}", phone);
        authService.sendSmsCode(phone);
        return ApiResponse.ok();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody Map<String, String> params) {
        log.info("【重置密码】手机号: {}", params.get("phone"));
        authService.resetPassword(params.get("phone"), params.get("smsCode"), params.get("newPassword"));
        return ApiResponse.ok();
    }
}
