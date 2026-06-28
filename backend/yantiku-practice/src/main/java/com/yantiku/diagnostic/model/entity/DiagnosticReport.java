package com.yantiku.module.diagnostic.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 诊断报告实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticReport {
    
    /**
     * 报告ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 科目ID
     */
    private Long subjectId;
    
    /**
     * 报告类型（默认"session"）
     */
    private String reportType;
    
    /**
     * 总体得分
     */
    private BigDecimal overallScore;
    
    /**
     * 能力评级
     */
    private String abilityRating;
    
    /**
     * 雷达图数据（JSON格式存储为String）
     */
    private String radarData;
    
    /**
     * 薄弱点（JSON格式存储为String）
     */
    private String weakPoints;
    
    /**
     * 优势点（JSON格式存储为String）
     */
    private String strengths;
    
    /**
     * 建议（JSON格式存储为String）
     */
    private String suggestions;
    
    /**
     * 时间分析（JSON格式存储为String）
     */
    private String timeAnalysis;
    
    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;
}
