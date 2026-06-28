package com.yantiku.module.auth.model.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新令牌实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    
    /**
     * 令牌ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 令牌哈希值
     */
    private String tokenHash;
    
    /**
     * 令牌族（用于检测令牌复用攻击）
     */
    private String family;
    
    /**
     * 设备信息
     */
    private String deviceInfo;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
    
    /**
     * 撤销时间
     */
    private LocalDateTime revokedAt;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
