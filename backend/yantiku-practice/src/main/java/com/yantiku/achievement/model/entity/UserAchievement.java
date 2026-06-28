package com.yantiku.module.achievement.model.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户成就实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievement {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 成就ID
     */
    private Long achievementId;
    
    /**
     * 进度（默认0）
     */
    private Integer progress;
    
    /**
     * 解锁时间
     */
    private LocalDateTime unlockedAt;
    
    /**
     * 通知时间
     */
    private LocalDateTime notifiedAt;
}
