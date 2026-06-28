package com.yantiku.module.question.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目搜索条件 DTO
 * <p>
 * 用于 QuestionMapper.searchQuestions / countQuestions 的分页筛选查询。
 * 所有筛选字段均为可选（null 表示不限制），offset/limit 控制分页。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSearchDTO {

    /** 科目 ID（可选） */
    private Long subjectId;

    /** 知识点 ID（可选） */
    private Long knowledgePointId;

    /** 题目类型：1=选择题 2=填空题 3=综合题（可选） */
    private Integer questionType;

    /** 难度等级 1-5（可选） */
    private Integer difficulty;

    /** 考题年份（可选） */
    private Integer examYear;

    /** 关键词搜索（可选，匹配题目内容） */
    private String keyword;

    /** 是否仅查询启用题目（默认 true） */
    @Builder.Default
    private Boolean activeOnly = true;

    /** 当前页码（从 1 开始，默认 1） */
    @Builder.Default
    private Integer page = 1;

    /** 每页数量（默认 20） */
    @Builder.Default
    private Integer pageSize = 20;

    /** 分页偏移量（计算字段） */
    public int getOffset() {
        return (page - 1) * pageSize;
    }

    /** 每页数量（别名方法，供 Mapper 使用） */
    public int getLimit() {
        return pageSize;
    }

    /** 设置偏移量（供 Mapper 兼容） */
    public void setOffset(Integer offset) {
        // 由 page 和 pageSize 计算，不存储
    }

    /** 设置每页数量（供 Mapper 兼容） */
    public void setLimit(Integer limit) {
        if (limit != null) {
            this.pageSize = limit;
        }
    }
}
