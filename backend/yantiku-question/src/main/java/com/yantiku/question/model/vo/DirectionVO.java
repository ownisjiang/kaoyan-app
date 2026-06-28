package com.yantiku.module.question.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 考研方向视图对象
 * 
 * 用于 API 响应，包含方向信息及其关联的科目列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectionVO {
    
    /**
     * 方向ID
     */
    private Long id;
    
    /**
     * 方向编码
     */
    private String code;
    
    /**
     * 方向名称
     */
    private String name;
    
    /**
     * 方向描述
     */
    private String description;
    
    /**
     * 院校数量
     */
    private Integer schoolCount;
    
    /**
     * 图标URL
     */
    private String iconUrl;
    
    /**
     * 是否激活
     */
    private Boolean isActive;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 科目列表
     */
    private List<SubjectVO> subjects;
}
