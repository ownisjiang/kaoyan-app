package com.yantiku.module.user.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 用户统计视图对象
 *
 * @author yantiku
 */
@Data
@Builder
public class UserStatsVO {

    /** 总答题数 */
    private int totalQuestions;

    /** 总正确数 */
    private int totalCorrect;

    /** 正确率 */
    private double accuracy;

    /** 总学习时长（秒） */
    private long totalDurationSec;

    /** 连续打卡天数 */
    private int streakDays;

    /** 今日答题数 */
    private int todayQuestions;

    /** 今日正确数 */
    private int todayCorrect;
}
