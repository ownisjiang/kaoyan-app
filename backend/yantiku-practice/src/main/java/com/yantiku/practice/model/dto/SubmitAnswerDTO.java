package com.yantiku.module.practice.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提交答案 DTO
 */
@Data
public class SubmitAnswerDTO {
    
    /**
     * 题目ID
     */
    @NotNull(message = "题目ID不能为空")
    private Long questionId;
    
    /**
     * 用户答案（JSON格式字符串）
     * 选择题: "A" 或 ["A", "B"]
     * 填空题: {"1": "答案1", "2": "答案2"}
     * 综合题: 文本内容
     */
    @NotBlank(message = "答案不能为空")
    private String userAnswer;
    
    /**
     * 答题耗时（秒）
     */
    private Integer timeSpent;
}
