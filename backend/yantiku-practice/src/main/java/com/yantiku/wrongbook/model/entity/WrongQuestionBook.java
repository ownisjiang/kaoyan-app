package com.yantiku.module.wrongbook.model.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 错题本实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WrongQuestionBook {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 题目ID
     */
    private Long questionId;
    
    /**
     * 科目ID（冗余，加速按科目查询错题）
     */
    private Long subjectId;
    
    /**
     * 错误次数（默认1）
     */
    private Integer wrongCount;
    
    /**
     * 连续正确次数（默认0）
     */
    private Integer consecutiveCorrect;
    
    /**
     * 最后错误时间
     */
    private LocalDateTime lastWrongAt;
    
    /**
     * 最后复习时间
     */
    private LocalDateTime lastReviewedAt;
    
    /**
     * 下次复习时间
     */
    private LocalDateTime nextReviewAt;
    
    /**
     * 掌握程度（默认0）
     */
    private Integer masteryLevel;
    
    /**
     * 状态（默认"active"）
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
