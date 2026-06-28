package com.yantiku.module.user.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户每日统计实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStats {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 统计日期
     */
    private LocalDate statDate;
    
    /**
     * 答题数（默认0）
     */
    private Integer questionsAnswered;
    
    /**
     * 正确数（默认0）
     */
    private Integer correctCount;
    
    /**
     * 学习时长（秒，默认0）
     */
    private Integer studyDurationSeconds;
    
    /**
     * 会话数（默认0）
     */
    private Integer sessionsCount;
    
    /**
     * 科目数据（JSON格式存储为String）
     */
    private String subjectsData;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
