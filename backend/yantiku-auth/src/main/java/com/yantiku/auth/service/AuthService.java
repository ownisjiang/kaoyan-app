package com.yantiku.module.auth.service;

import com.yantiku.common.dto.ApiResponse;
import com.yantiku.common.exception.BusinessException;
import com.yantiku.common.exception.ErrorCode;
import com.yantiku.module.auth.mapper.RefreshTokenMapper;
import com.yantiku.module.auth.model.dto.LoginDTO;
import com.yantiku.module.auth.model.dto.RefreshTokenDTO;
import com.yantiku.module.auth.model.dto.RegisterDTO;
import com.yantiku.module.auth.model.entity.RefreshToken;
import com.yantiku.module.auth.model.vo.AuthVO;
import com.yantiku.module.user.mapper.UserMapper;
import com.yantiku.module.user.model.entity.User;
import com.yantiku.module.user.model.vo.UserVO;
import com.yantiku.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 认证服务
 *
 * @author yantiku
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final ConcurrentHashMap<String, String> smsCodeStore = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 用户注册
     *
     * @param dto 注册信息
     * @return 认证信息
     */
    @Transactional(rollbackFor = Exception.class)
    public AuthVO register(RegisterDTO dto) {
        log.info("用户注册: {}", dto.getPhone());

        // 检查手机号是否已注册
        User existUser = userMapper.findByPhone(dto.getPhone());
        if (existUser != null) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_REGISTERED);
        }

        // 创建新用户
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setTargetDirectionId(dto.getTargetDirectionId());
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setTotalQuestions(0);
        user.setTotalCorrect(0);
        user.setTotalDurationSec(0L);
        user.setStreakDays(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户注册成功: userId={}, phone={}", user.getId(), user.getPhone());

        // 生成令牌
        return generateTokens(user);
    }

    /**
     * 用户登录
     *
     * @param dto 登录信息
     * @return 认证信息
     */
    public AuthVO login(LoginDTO dto) {
        log.info("用户登录: {}", dto.getPhone());

        // 查找用户
        User user = userMapper.findByPhone(dto.getPhone());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 检查账号状态
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_ACCOUNT_DISABLED);
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.update(user);

        log.info("用户登录成功: userId={}", user.getId());

        // 生成令牌
        return generateTokens(user);
    }

    /**
     * 刷新令牌
     *
     * @param refreshTokenStr 刷新令牌
     * @return 新的认证信息
     */
    @Transactional(rollbackFor = Exception.class)
    public AuthVO refreshToken(String refreshTokenStr) {
        log.info("刷新令牌");

        // 验证刷新令牌
        Long userId;
        try {
            userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshTokenStr);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 查找刷新令牌记录（通过令牌哈希）
        String tokenHash = hashToken(refreshTokenStr);
        RefreshToken refreshTokenEntity = refreshTokenMapper.findByTokenHash(tokenHash);
        if (refreshTokenEntity == null) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 检查令牌是否已撤销
        if (refreshTokenEntity.getRevokedAt() != null) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }

        // 检查令牌是否过期
        if (refreshTokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // 撤销旧的刷新令牌（令牌轮换）
        refreshTokenMapper.revokeToken(refreshTokenEntity.getId());

        // 查找用户
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查账号状态
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_ACCOUNT_DISABLED);
        }

        log.info("令牌刷新成功: userId={}", userId);

        // 生成新的令牌
        return generateTokens(user);
    }

    /**
     * 退出登录
     *
     * @param refreshToken 刷新令牌
     */
    @Transactional(rollbackFor = Exception.class)
    public void logout(String refreshToken) {
        log.info("用户退出登录");

        // 通过哈希查找并撤销刷新令牌
        String tokenHash = hashToken(refreshToken);
        RefreshToken tokenEntity = refreshTokenMapper.findByTokenHash(tokenHash);
        if (tokenEntity != null) {
            refreshTokenMapper.revokeToken(tokenEntity.getId());
        }
        log.info("刷新令牌已撤销");
    }

    /**
     * 发送短信验证码（随机生成6位数字）
     *
     * @param phone 手机号
     */
    public void sendSmsCode(String phone) {
        log.info("发送短信验证码到手机号: {}", phone);
        String code = String.format("%06d", (int)(Math.random() * 1000000));
        smsCodeStore.put(phone, code);
        log.info("【DEV】验证码: {} (手机号: {}), 生产环境需接入短信服务", code, phone);
    }

    public void resetPassword(String phone, String smsCode, String newPassword) {
        log.info("重置密码: phone={}", phone);
        User user = userMapper.findByPhone(phone);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        String stored = smsCodeStore.get(phone);
        if (stored == null || !stored.equals(smsCode)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_ERROR);
        }
        smsCodeStore.remove(phone);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.update(user);
    }

    /**
     * 生成访问令牌和刷新令牌
     *
     * @param user 用户实体
     * @return 认证信息
     */
    private AuthVO generateTokens(User user) {
        // 生成访问令牌
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
        long expiresIn = jwtTokenProvider.getAccessTokenExpiration();

        // 生成刷新令牌
        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId());

        // 保存刷新令牌到数据库（存储哈希值，不存储原始令牌）
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setTokenHash(hashToken(refreshTokenStr));
        refreshTokenEntity.setUserId(user.getId());
        refreshTokenEntity.setFamily(UUID.randomUUID().toString());
        refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration() / 1000));
        refreshTokenEntity.setCreatedAt(LocalDateTime.now());

        refreshTokenMapper.insert(refreshTokenEntity);

        // 构建用户信息
        UserVO userVO = UserVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .targetDirectionId(user.getTargetDirectionId())
                .targetSchool(user.getTargetSchool())
                .examYear(user.getExamYear())
                .role(user.getRole())
                .status(user.getStatus())
                .totalQuestions(user.getTotalQuestions())
                .totalCorrect(user.getTotalCorrect())
                .totalDurationSec(user.getTotalDurationSec())
                .streakDays(user.getStreakDays())
                .lastActiveDate(user.getLastActiveDate())
                .build();

        // 构建认证信息
        return AuthVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(userVO)
                .build();
    }

    /**
     * 对令牌进行 SHA-256 哈希，用于安全存储和查询
     *
     * @param token 原始令牌
     * @return 哈希后的十六进制字符串
     */
    private String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
