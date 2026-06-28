package com.yantiku.module.diagnostic.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 诊断报告视图对象
 *
 * 用于诊断报告展示，JSON 字段以 Object 形式返回供前端直接渲染
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticReportVO {

    /** 报告ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 会话ID */
    private Long sessionId;

    /** 科目ID */
    private Long subjectId;

    /** 报告类型（session/weekly/monthly） */
    private String reportType;

    /** 总体得分 */
    private BigDecimal overallScore;

    /** 能力评级（S/A/B/C/D） */
    private String abilityRating;

    /** 雷达图数据（各维度得分） */
    private Object radarData;

    /** 薄弱点列表 */
    private Object weakPoints;

    /** 优势点列表 */
    private Object strengths;

    /** 改进建议 */
    private Object suggestions;

    /** 时间分析 */
    private Object timeAnalysis;

    /** 生成时间 */
    private LocalDateTime generatedAt;
}
