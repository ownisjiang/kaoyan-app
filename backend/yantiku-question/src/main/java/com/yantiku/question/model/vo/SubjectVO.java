package com.yantiku.module.question.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 科目视图对象
 * 
 * 用于 API 响应，包含科目信息、掌握度、题型分布及其关联的知识点列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectVO {
    
    /**
     * 科目ID
     */
    private Long id;
    
    /**
     * 方向ID
     */
    private Long directionId;
    
    /**
     * 科目名称
     */
    private String name;
    
    /**
     * 科目代码（如 CS01, MATH01）
     */
    private String code;
    
    /**
     * 图标URL
     */
    private String iconUrl;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 掌握度（0-100，基于该科目标题正确率）
     */
    private Integer mastery;
    
    /**
     * 题型分布统计（选择题X题、填空题Y题等）
     */
    private List<QuestionTypeCount> questionTypes;
    
    /**
     * 知识点列表
     */
    private List<KnowledgePointVO> knowledgePoints;
    
    /**
     * 题型统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionTypeCount {
        /** 题目类型编码（1=选择题, 2=填空题, 3=综合题） */
        private String type;
        /** 题目类型中文标签 */
        private String label;
        /** 该类型题目数量 */
        private Integer count;
    }
}
