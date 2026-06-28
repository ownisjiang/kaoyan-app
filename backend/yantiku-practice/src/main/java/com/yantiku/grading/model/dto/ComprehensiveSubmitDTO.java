package com.yantiku.module.grading.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 综合题提交 DTO
 */
@Data
public class ComprehensiveSubmitDTO {
    
    /**
     * 题目ID
     */
    @NotNull(message = "题目ID不能为空")
    private Long questionId;
    
    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;
    
    /**
     * 用户答案
     */
    @NotBlank(message = "答案不能为空")
    private String userAnswer;
    
    /**
     * 题目类型（默认comprehensive）
     */
    @NotBlank(message = "题目类型不能为空")
    private String questionType = "comprehensive";
}
