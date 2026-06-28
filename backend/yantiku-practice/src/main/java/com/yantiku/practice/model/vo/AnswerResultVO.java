package com.yantiku.module.practice.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 答题结果视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResultVO {
    
    /**
     * 答题记录ID
     */
    private Long answerId;
    
    /**
     * 是否正确
     */
    @JsonProperty("isCorrect")
    private boolean isCorrect;
    
    /**
     * 得分
     */
    private BigDecimal score;
    
    /**
     * 满分
     */
    private BigDecimal maxScore;
    
    /**
     * 正确答案（从题目答案解析）
     */
    private Object correctAnswer;
    
    /**
     * 题目解析
     */
    private String analysis;
    
    /**
     * AI反馈（综合题评判结果）
     */
    private Object aiFeedback;
}
