package com.yantiku.module.achievement.controller;

import com.yantiku.common.dto.ApiResponse;
import com.yantiku.module.achievement.model.vo.AchievementVO;
import com.yantiku.module.achievement.service.AchievementService;
import com.yantiku.common.annotation.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成就控制器
 *
 * 提供成就查询、成就检查与解锁接口
 */
@RestController
@RequestMapping("/api/v1/achievements")
@RequiredArgsConstructor
@Slf4j
public class AchievementController {

    private final AchievementService achievementService;

    /**
     * 获取用户成就列表
     *
     * GET /api/v1/achievements
     *
     * @param userId 当前用户 ID
     * @return 成就列表（含进度和解锁状态）
     */
    @GetMapping
    public ApiResponse<List<AchievementVO>> getUserAchievements(@CurrentUser Long userId) {
        log.info("【获取用户成就】userId={}", userId);
        List<AchievementVO> achievements = achievementService.getUserAchievements(userId);
        return ApiResponse.ok(achievements);
    }

    /**
     * 检查并解锁成就
     *
     * POST /api/v1/achievements/check
     *
     * @param userId 当前用户 ID
     * @return 操作结果
     */
    @PostMapping("/check")
    public ApiResponse<Void> checkAndUnlockAchievements(@CurrentUser Long userId) {
        log.info("【检查并解锁成就】userId={}", userId);
        achievementService.checkAndUnlockAchievements(userId);
        return ApiResponse.ok(null);
    }
}
