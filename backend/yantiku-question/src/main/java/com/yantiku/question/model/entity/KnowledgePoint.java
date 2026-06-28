package com.yantiku.module.question.model.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识点实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePoint {
    
    /**
     * 知识点ID
     */
    private Long id;
    
    /**
     * 科目ID
     */
    private Long subjectId;
    
    /**
     * 父知识点ID
     */
    private Long parentId;
    
    /**
     * 知识点名称
     */
    private String name;
    
    /**
     * 知识点层级
     */
    private Integer level;
    
    /**
     * 排序顺序
     */
    private Integer sort;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
