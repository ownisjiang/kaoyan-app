package com.yantiku.module.grading.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 判分结果视图对象
 * 用于 WebSocket 推送到前端
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingResultVO {

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 题目类型（single_choice / multi_choice / fill_blank / comprehensive）
     */
    private String questionType;

    /**
     * 是否正确（客观题）
     */
    private Boolean correct;

    /**
     * 获得分数
     */
    private Integer obtainedScore;

    /**
     * 题目总分
     */
    private Integer totalScore;

    /**
     * 判分状态（success / pending_review / failed）
     */
    private String status;

    /**
     * 反馈信息（错误原因、解析等）
     */
    private String feedback;

    /**
     * 标准答案（填空用）
     */
    private String standardAnswer;

    /**
     * 综合题 AI 判分详情（异步判分完成后填充）
     */
    private String aiGradingDetail;
}
