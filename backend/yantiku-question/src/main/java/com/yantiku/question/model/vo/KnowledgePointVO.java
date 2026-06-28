package com.yantiku.module.question.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识点视图对象
 * 
 * 用于 API 响应，支持树形结构（父子关系）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePointVO {
    
    /**
     * 知识点ID
     */
    private Long id;
    
    /**
     * 科目ID
     */
    private Long subjectId;
    
    /**
     * 父知识点ID（null 表示根节点）
     */
    private Long parentId;
    
    /**
     * 知识点名称
     */
    private String name;
    
    /**
     * 知识点层级（1=一级知识点，2=二级知识点，3=三级知识点）
     */
    private Integer level;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 子知识点列表（用于树形结构）
     */
    private List<KnowledgePointVO> children;
}
