package com.yantiku.module.practice.model.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.yantiku.module.question.model.vo.QuestionVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 练习会话视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionVO {
    
    /**
     * 会话ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 科目ID
     */
    private Long subjectId;
    
    /**
     * 练习模式
     */
    private String mode;
    
    /**
     * 总题目数
     */
    private int totalQuestions;
    
    /**
     * 已答题数
     */
    private int answeredCount;
    
    /**
     * 正确数
     */
    private int correctCount;
    
    /**
     * 总得分
     */
    private BigDecimal totalScore;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 持续时间（秒）
     */
    private Integer durationSeconds;
    
    /**
     * 状态（in_progress/completed/abandoned/paused）
     */
    private String status;

    private List<QuestionVO> questions;
}
