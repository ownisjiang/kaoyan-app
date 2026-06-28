package com.yantiku.module.achievement.service;

import com.yantiku.module.achievement.mapper.AchievementMapper;
import com.yantiku.module.achievement.mapper.UserAchievementMapper;
import com.yantiku.module.achievement.model.entity.Achievement;
import com.yantiku.module.achievement.model.entity.UserAchievement;
import com.yantiku.module.achievement.model.vo.AchievementVO;
import com.yantiku.module.user.mapper.UserMapper;
import com.yantiku.module.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 成就服务
 *
 * 提供成就查询、进度合并、自动解锁检测等功能。
 * 成就条件类型：
 * <ul>
 *   <li>consecutive_days — 连续打卡天数</li>
 *   <li>total_questions — 累计答题数</li>
 *   <li>accuracy_rate — 正确率（需至少答题 100 道）</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final UserMapper userMapper;

    /**
     * 获取用户成就列表
     * <p>
     * 合并成就定义表与用户进度表，返回包含进度和解锁状态的完整列表
     *
     * @param userId 用户 ID
     * @return 成就列表
     */
    public List<AchievementVO> getUserAchievements(Long userId) {
        log.info("获取用户成就: userId={}", userId);

        // 获取全部成就定义
        List<Achievement> definitions = achievementMapper.findAll();

        // 获取用户已有进度
        List<UserAchievement> userAchievements = userAchievementMapper.findByUserId(userId);
        Map<Long, UserAchievement> progressMap = userAchievements.stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, Function.identity(), (a, b) -> a));

        // 合并为 VO
        return definitions.stream()
                .map(achievement -> {
                    UserAchievement ua = progressMap.get(achievement.getId());
                    return convertToVO(achievement, ua);
                })
                .collect(Collectors.toList());
    }

    /**
     * 检查并解锁用户成就
     * <p>
     * 获取用户统计数据（总答题数、总正确数、连续打卡天数、正确率），
     * 遍历所有成就定义，根据条件类型计算进度并 upsert。
     *
     * @param userId 用户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkAndUnlockAchievements(Long userId) {
        log.info("检查并解锁成就: userId={}", userId);

        User user = userMapper.findById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            return;
        }

        // 获取用户统计
        int totalQuestions = user.getTotalQuestions() != null ? user.getTotalQuestions() : 0;
        int totalCorrect = user.getTotalCorrect() != null ? user.getTotalCorrect() : 0;
        int streakDays = user.getStreakDays() != null ? user.getStreakDays() : 0;
        double accuracy = totalQuestions > 0
                ? (double) totalCorrect / totalQuestions * 100
                : 0.0;

        log.info("用户统计: userId={}, totalQuestions={}, totalCorrect={}, streakDays={}, accuracy={}",
                userId, totalQuestions, totalCorrect, streakDays, accuracy);

        // 获取全部成就定义
        List<Achievement> definitions = achievementMapper.findAll();

        for (Achievement achievement : definitions) {
            int progress = calculateProgress(achievement, totalQuestions, streakDays, accuracy);

            UserAchievement ua = UserAchievement.builder()
                    .userId(userId)
                    .achievementId(achievement.getId())
                    .progress(progress)
                    .build();

            try {
                userAchievementMapper.upsert(ua);
            } catch (Exception e) {
                log.error("成就进度更新失败: userId={}, achievementCode={}, error={}",
                        userId, achievement.getCode(), e.getMessage());
            }
        }

        log.info("成就检查完成: userId={}", userId);
    }

    // ========================================================
    // 私有方法
    // ========================================================

    /**
     * 根据条件类型计算成就进度
     *
     * @param achievement     成就定义
     * @param totalQuestions  总答题数
     * @param streakDays      连续打卡天数
     * @param accuracy        正确率
     * @return 进度（0-100）
     */
    private int calculateProgress(Achievement achievement, int totalQuestions,
                                   int streakDays, double accuracy) {
        String conditionType = achievement.getConditionType();
        int conditionValue = achievement.getConditionValue() != null ? achievement.getConditionValue() : 0;

        if (conditionValue <= 0) {
            return 0;
        }

        int currentValue;
        int progress;

        switch (conditionType) {
            case "consecutive_days":
                currentValue = streakDays;
                progress = currentValue >= conditionValue ? 100
                        : Math.min(99, currentValue * 100 / conditionValue);
                break;

            case "total_questions":
                currentValue = totalQuestions;
                progress = currentValue >= conditionValue ? 100
                        : Math.min(99, currentValue * 100 / conditionValue);
                break;

            case "accuracy_rate":
                // 正确率成就需要至少答题 100 道
                if (totalQuestions < 100) {
                    progress = Math.min(49, totalQuestions * 50 / 100);
                } else {
                    progress = accuracy >= conditionValue ? 100
                            : Math.min(99, (int) (accuracy * 100 / conditionValue));
                }
                break;

            default:
                log.warn("未知成就条件类型: code={}, conditionType={}", achievement.getCode(), conditionType);
                progress = 0;
        }

        log.debug("成就进度计算: code={}, conditionType={}, conditionValue={}, progress={}",
                achievement.getCode(), conditionType, conditionValue, progress);
        return progress;
    }

    /**
     * 将成就定义 + 用户进度合并为 VO
     */
    private AchievementVO convertToVO(Achievement achievement, UserAchievement userAchievement) {
        int progress = userAchievement != null && userAchievement.getProgress() != null
                ? userAchievement.getProgress() : 0;
        boolean unlocked = userAchievement != null && userAchievement.getUnlockedAt() != null;

        return AchievementVO.builder()
                .achievementId(achievement.getId())
                .code(achievement.getCode())
                .name(achievement.getName())
                .description(achievement.getDescription())
                .iconUrl(achievement.getIconUrl())
                .category(achievement.getCategory())
                .conditionType(achievement.getConditionType())
                .conditionValue(achievement.getConditionValue() != null ? achievement.getConditionValue() : 0)
                .progress(progress)
                .unlocked(unlocked)
                .unlockedAt(userAchievement != null ? userAchievement.getUnlockedAt() : null)
                .build();
    }
}
