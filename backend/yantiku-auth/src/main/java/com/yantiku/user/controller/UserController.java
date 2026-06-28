package com.yantiku.module.user.controller;

import com.yantiku.common.dto.ApiResponse;
import com.yantiku.module.user.model.dto.UpdateUserDTO;
import com.yantiku.module.user.model.entity.DailyStats;
import com.yantiku.module.user.model.vo.UserStatsVO;
import com.yantiku.module.user.model.vo.UserVO;
import com.yantiku.module.user.service.UserService;
import com.yantiku.common.annotation.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 *
 * @author yantiku
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     *
     * @param userId 当前用户ID
     * @return 用户信息
     */
    @GetMapping("/me")
    public ApiResponse<UserVO> getCurrentUser(@CurrentUser Long userId) {
        log.info("【获取当前用户信息】userId={}", userId);
        UserVO userVO = userService.getCurrentUser(userId);
        return ApiResponse.ok(userVO);
    }

    /**
     * 更新当前用户信息
     *
     * @param userId 当前用户ID
     * @param dto    更新信息
     * @return 更新后的用户信息
     */
    @PutMapping("/me")
    public ApiResponse<UserVO> updateUser(
            @CurrentUser Long userId,
            @Validated @RequestBody UpdateUserDTO dto) {
        log.info("【更新当前用户信息】userId={}", userId);
        UserVO userVO = userService.updateUser(userId, dto);
        return ApiResponse.ok(userVO);
    }

    /**
     * 获取当前用户统计信息
     *
     * @param userId 当前用户ID
     * @return 用户统计信息
     */
    @GetMapping("/me/stats")
    public ApiResponse<UserStatsVO> getUserStats(@CurrentUser Long userId) {
        log.info("【获取当前用户统计信息】userId={}", userId);
        UserStatsVO statsVO = userService.getUserStats(userId);
        return ApiResponse.ok(statsVO);
    }

    /**
     * 获取当前用户每日统计
     *
     * @param userId 当前用户ID
     * @param days   天数
     * @return 每日统计列表
     */
    @GetMapping("/me/stats/daily")
    public ApiResponse<List<DailyStats>> getDailyStats(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "30") int days) {
        log.info("【获取当前用户每日统计】userId={}, days={}", userId, days);
        List<DailyStats> dailyStats = userService.getDailyStats(userId, days);
        return ApiResponse.ok(dailyStats);
    }

    /**
     * 获取当前用户成就
     *
     * @param userId 当前用户ID
     * @return 用户成就列表
     */
    @GetMapping("/me/achievements")
    public ApiResponse<List<Map<String, Object>>> getUserAchievements(@CurrentUser Long userId) {
        log.info("【获取当前用户成就】userId={}", userId);
        List<Map<String, Object>> achievements = userService.getUserAchievements(userId);
        return ApiResponse.ok(achievements);
    }

    /**
     * 更新当前用户目标
     *
     * @param userId            当前用户ID
     * @param targetDirectionId 目标方向ID
     * @param targetSchool      目标学校
     * @return 更新后的用户信息
     */
    @PutMapping("/me/target")
    public ApiResponse<UserVO> updateTarget(
            @CurrentUser Long userId,
            @RequestParam(required = false) Long targetDirectionId,
            @RequestParam(required = false) String targetSchool) {
        log.info("【更新当前用户目标】userId={}, targetDirectionId={}, targetSchool={}",
                userId, targetDirectionId, targetSchool);

        // 构建更新DTO
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setTargetDirectionId(targetDirectionId);
        dto.setTargetSchool(targetSchool);

        UserVO userVO = userService.updateUser(userId, dto);
        return ApiResponse.ok(userVO);
    }

    /**
     * 删除当前用户（软删除）
     *
     * @param userId 当前用户ID
     * @return 成功响应
     */
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteUser(@CurrentUser Long userId) {
        log.info("【删除当前用户】userId={}", userId);
        userService.deleteUser(userId);
        return ApiResponse.ok();
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> changePassword(
            @CurrentUser Long userId,
            @RequestBody Map<String, String> params) {
        log.info("【修改密码】userId={}", userId);
        userService.changePassword(userId, params.get("oldPassword"), params.get("newPassword"));
        return ApiResponse.ok();
    }

    @GetMapping("/me/timeline")
    public ApiResponse<List<Map<String, Object>>> getTimeline(@CurrentUser Long userId) {
        log.info("【获取学习时间线】userId={}", userId);
        return ApiResponse.ok(List.of());
    }
}
