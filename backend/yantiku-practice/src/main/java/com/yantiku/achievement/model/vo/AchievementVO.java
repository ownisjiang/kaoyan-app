package com.yantiku.module.achievement.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 成就视图对象
 *
 * 合并成就定义与用户进度，用于成就列表展示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementVO {

    /** 成就ID */
    private Long achievementId;

    /** 成就编码 */
    private String code;

    /** 成就名称 */
    private String name;

    /** 成就描述 */
    private String description;

    /** 图标URL */
    private String iconUrl;

    /** 成就分类（streak/answer/accuracy） */
    private String category;

    /** 条件类型（consecutive_days/total_questions/accuracy_rate） */
    private String conditionType;

    /** 条件值 */
    private int conditionValue;

    /** 当前进度（0-100） */
    private int progress;

    /** 是否已解锁 */
    private boolean unlocked;

    /** 解锁时间 */
    private LocalDateTime unlockedAt;
}
