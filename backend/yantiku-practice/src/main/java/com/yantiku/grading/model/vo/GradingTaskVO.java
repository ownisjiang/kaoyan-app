package com.yantiku.module.grading.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 判分任务视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingTaskVO {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 任务状态（pending/in_progress/completed/failed）
     */
    private String status;
    
    /**
     * 预计完成时间（秒）
     */
    private int estimatedSeconds;
}
