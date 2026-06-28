package com.yantiku.module.user.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户视图对象
 *
 * @author yantiku
 */
@Data
@Builder
public class UserVO {

    /** 用户ID */
    private Long id;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatarUrl;

    /** 目标方向ID */
    private Long targetDirectionId;

    /** 目标学校 */
    private String targetSchool;

    /** 考试年份 */
    private Integer examYear;

    /** 角色 */
    private String role;

    /** 状态 */
    private String status;

    /** 总答题数 */
    private Integer totalQuestions;

    /** 总正确数 */
    private Integer totalCorrect;

    /** 总学习时长（秒） */
    private Long totalDurationSec;

    /** 连续打卡天数 */
    private Integer streakDays;

    /** 最后活跃日期 */
    private LocalDate lastActiveDate;
}
