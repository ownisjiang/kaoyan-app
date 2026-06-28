package com.yantiku.module.question.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目实体类
 * 
 * questionType: 1=选择题 2=填空题 3=综合题
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    
    /**
     * 题目ID
     */
    private Long id;
    
    /**
     * 科目ID
     */
    private Long subjectId;
    
    /**
     * 知识点ID
     */
    private Long knowledgePointId;
    
    /**
     * 题目类型（1=选择题 2=填空题 3=综合题）
     */
    private Integer questionType;
    
    private Integer difficulty;
    
    private Integer examYear;
    
    private String source;
    
    private String content;
    
    /**
     * 选项（JSON 字符串数组，仅选择题有）
     * 格式: ["A. xxx", "B. xxx", ...]
     */
    private String options;
    
    private String answer;
    
    private String analysis;
    
    /**
     * 标签（JSON格式存储为String）
     */
    private String tags;
    
    /**
     * 使用次数
     */
    private Integer useCount;
    
    /**
     * 正确次数
     */
    private Integer correctCount;
    
    /**
     * 正确率
     */
    private BigDecimal correctRate;
    
    /**
     * 是否激活
     */
    private Boolean isActive;
    
    /**
     * 创建者ID
     */
    private Long createdBy;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
