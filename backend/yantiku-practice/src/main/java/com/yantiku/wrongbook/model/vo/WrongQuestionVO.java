package com.yantiku.module.wrongbook.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 错题本视图对象
 *
 * 用于错题列表展示，包含错题记录信息及题目简要信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WrongQuestionVO {

    /** 错题记录ID */
    private Long id;

    /** 题目ID */
    private Long questionId;

    /** 知识点ID */
    private Long knowledgePointId;

    /** 知识点名称 */
    private String knowledgePointName;

    /** 错误次数 */
    private int wrongCount;

    /** 连续正确次数 */
    private int consecutiveCorrect;

    /** 最后错误时间 */
    private LocalDateTime lastWrongAt;

    /** 最后复习时间 */
    private LocalDateTime lastReviewedAt;

    /** 下次复习时间 */
    private LocalDateTime nextReviewAt;

    /** 掌握等级（0-5） */
    private int masteryLevel;

    /** 状态（active/archived/mastered） */
    private String status;

    /** 题目类型（1=选择题 2=填空题 3=综合题） */
    private Integer questionType;

    /** 难度等级（1-5） */
    private Integer difficulty;

    /** 题目内容预览（前100字符） */
    private String contentPreview;

    /** 选择题选项（JSON数组） */
    private String options;

    /** 题目完整内容 */
    private String content;

    /** 参考答案 */
    private String answer;
