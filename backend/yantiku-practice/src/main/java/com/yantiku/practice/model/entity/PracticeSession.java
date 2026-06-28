package com.yantiku.module.practice.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 练习会话实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeSession {
    
    /**
     * 会话ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 科目ID
     */
    private Long subjectId;
    
    /**
     * 练习模式
     */
    private String mode;
    
    /**
     * 总题目数
     */
    private Integer totalQuestions;
    
    /**
     * 已答题数
     */
    private Integer answeredCount;
    
    /**
     * 正确数
     */
    private Integer correctCount;
    
    /**
     * 总得分
     */
    private BigDecimal totalScore;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 持续时间（秒）
     */
    private Integer durationSeconds;
    
    /**
     * 状态（默认"in_progress"）
     */
    private String status;
    
    /**
     * 配置信息（JSON格式存储为String）
     */
    private String config;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
