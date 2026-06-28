package com.yantiku.module.practice.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户答题记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswer {
    
    /**
     * 答题记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 题目ID
     */
    private Long questionId;
    
    /**
     * 用户答案（JSON格式存储为String）
     */
    private String userAnswer;
    
    /**
     * 是否正确
     */
    private Boolean isCorrect;
    
    /**
     * 得分
     */
    private BigDecimal score;
    
    /**
     * 最高得分
     */
    private BigDecimal maxScore;
    
    /**
     * 花费时间（秒）
     */
    private Integer timeSpent;
    
    /**
     * 是否收藏（默认false）
     */
    private Boolean isCollected;
    
    /**
     * AI反馈（JSON格式存储为String）
     */
    private String aiFeedback;
    
    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;
}
