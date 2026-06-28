package com.yantiku.module.achievement.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成就实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {
    
    /**
     * 成就ID
     */
    private Long id;
    
    /**
     * 成就编码
     */
    private String code;
    
    /**
     * 成就名称
     */
    private String name;
    
    /**
     * 成就描述
     */
    private String description;
    
    /**
     * 图标URL
     */
    private String iconUrl;
    
    /**
     * 成就类别
     */
    private String category;
    
    /**
     * 条件类型
     */
    private String conditionType;
    
    /**
     * 条件值
     */
    private Integer conditionValue;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
}
