package com.yantiku.module.practice.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建练习会话 DTO
 */
@Data
public class CreateSessionDTO {
    
    /**
     * 科目ID
     */
    private Long subjectId;
    
    /**
     * 练习模式
     * quick_quiz=快速刷题, fill_blank=填空题, comprehensive=综合题, mock_exam=模拟考试
     */
    @NotBlank(message = "练习模式不能为空")
    private String mode;
    
    /**
     * 总题目数
     */
    @NotNull(message = "题目数不能为空")
    @Min(value = 1, message = "题目数最少为1")
    @Max(value = 200, message = "题目数最多为200")
    private Integer totalQuestions;
    
    /**
     * 配置信息（JSON格式，用于模拟考试等场景）
     */
    private String config;
}
