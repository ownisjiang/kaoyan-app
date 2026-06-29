package com.yantiku.module.question.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建/更新题目 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionDTO {

    @NotNull(message = "科目不能为空")
    private Long subjectId;

    private Long knowledgePointId;

    @NotNull(message = "题目类型不能为空")
    private Integer questionType; // 1=选择 2=填空 3=综合

    private Integer difficulty;

    private Integer examYear;

    private String source;

    @NotBlank(message = "题目内容不能为空")
    private String content;

    /** 选择题选项（JSON数组字符串） */
    private String options;

    @NotBlank(message = "参考答案不能为空")
    private String answer;

    private String analysis;

    /** 标签（逗号分隔） */
    private String tags;
}
