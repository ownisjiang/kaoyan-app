package com.yantiku.module.user.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 密码哈希
     */
    private String passwordHash;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 目标方向ID
     */
    private Long targetDirectionId;
    
    /**
     * 目标院校
     */
    private String targetSchool;
    
    /**
     * 考研年份
     */
    private Integer examYear;
    
    /**
     * 角色（默认"student"）
     */
    private String role;
    
    /**
     * 状态（默认"active"）
     */
    private String status;
    
    /**
     * 总答题数
     */
    private Integer totalQuestions;
    
    /**
     * 总正确数
     */
    private Integer totalCorrect;
    
    /**
     * 总学习时长（秒）
     */
    private Long totalDurationSec;
    
    /**
     * 连续打卡天数
     */
    private Integer streakDays;
    
    /**
     * 最后活跃日期
     */
    private LocalDate lastActiveDate;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 删除时间（软删除）
     */
    private LocalDateTime deletedAt;
}
