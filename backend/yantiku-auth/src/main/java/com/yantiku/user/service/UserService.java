package com.yantiku.module.user.service;

import com.yantiku.common.dto.ApiResponse;
import com.yantiku.common.exception.BusinessException;
import com.yantiku.common.exception.ErrorCode;
import com.yantiku.module.user.mapper.DailyStatsMapper;
import com.yantiku.module.user.mapper.UserMapper;
import com.yantiku.module.user.model.dto.UpdateUserDTO;
import com.yantiku.module.user.model.entity.DailyStats;
import com.yantiku.module.user.model.entity.User;
import com.yantiku.module.user.model.vo.UserStatsVO;
import com.yantiku.module.user.model.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户服务
 *
 * @author yantiku
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final DailyStatsMapper dailyStatsMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public UserVO getCurrentUser(Long userId) {
        log.info("获取用户信息: userId={}", userId);

        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        return convertToUserVO(user);
    }

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param dto    更新信息
     * @return 更新后的用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUser(Long userId, UpdateUserDTO dto) {
        log.info("更新用户信息: userId={}", userId);

        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 只更新非null字段
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        if (dto.getTargetDirectionId() != null) {
            user.setTargetDirectionId(dto.getTargetDirectionId());
        }
        if (dto.getTargetSchool() != null) {
            user.setTargetSchool(dto.getTargetSchool());
        }
        if (dto.getExamYear() != null) {
            user.setExamYear(dto.getExamYear());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);

        log.info("用户信息更新成功: userId={}", userId);
        return convertToUserVO(user);
    }

    /**
     * 获取用户统计信息
     *
     * @param userId 用户ID
     * @return 用户统计信息
     */
    public UserStatsVO getUserStats(Long userId) {
        log.info("获取用户统计信息: userId={}", userId);

        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 获取今日统计
        LocalDate today = LocalDate.now();
        DailyStats todayStats = dailyStatsMapper.findByUserIdAndDate(userId, today);

        int todayQuestions = todayStats != null ? todayStats.getQuestionsAnswered() : 0;
        int todayCorrect = todayStats != null ? todayStats.getCorrectCount() : 0;

        // 计算正确率
        double accuracy = user.getTotalQuestions() > 0
                ? (double) user.getTotalCorrect() / user.getTotalQuestions() * 100
                : 0.0;

        return UserStatsVO.builder()
                .totalQuestions(user.getTotalQuestions())
                .totalCorrect(user.getTotalCorrect())
                .accuracy(accuracy)
                .totalDurationSec(user.getTotalDurationSec())
                .streakDays(user.getStreakDays())
                .todayQuestions(todayQuestions)
                .todayCorrect(todayCorrect)
                .build();
    }

    /**
     * 获取用户每日统计
     *
     * @param userId 用户ID
     * @param days   天数
     * @return 每日统计列表
     */
    public List<DailyStats> getDailyStats(Long userId, int days) {
        log.info("获取用户每日统计: userId={}, days={}", userId, days);

        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        return dailyStatsMapper.findByUserId(userId, startDate, LocalDate.now());
    }

    /**
     * 获取用户成就
     *
     * @param userId 用户ID
     * @return 用户成就列表
     */
    public List<Map<String, Object>> getUserAchievements(Long userId) {
        log.info("获取用户成就: userId={}", userId);
        // 成就模块已迁移到 yantiku-practice 服务
        // 前端应直接调用 /api/v1/achievements
        return Collections.emptyList();
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.update(user);
    }

    /**
     * 软删除用户
     */
    public void deleteUser(Long userId) {
        int rows = userMapper.softDelete(userId);
        if (rows == 0) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        log.info("用户已软删除: userId={}", userId);
    }

    /**
     * 转换用户实体为VO
     *
     * @param user 用户实体
     * @return 用户VO
     */
    private UserVO convertToUserVO(User user) {
        return UserVO.builder()
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
    }
}
