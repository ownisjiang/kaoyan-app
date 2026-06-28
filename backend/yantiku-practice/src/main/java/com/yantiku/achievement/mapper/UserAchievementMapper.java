package com.yantiku.module.achievement.mapper;

import com.yantiku.module.achievement.model.entity.UserAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户成就 Mapper 接口
 *
 * upsert（ON DUPLICATE KEY UPDATE）通过 XML 实现，
 * 对应文件：mapper/achievement/UserAchievementMapper.xml
 */
@Mapper
public interface UserAchievementMapper {

    /**
     * 插入或更新用户成就进度 — XML 实现
     * <p>
     * 利用 UNIQUE KEY uk_user_achievement (user_id, achievement_id) 触发
     * ON DUPLICATE KEY UPDATE：更新 progress 字段。
     * <p>
     * 当 progress &gt;= condition_value 且 unlocked_at IS NULL 时，
     * 由 XML 中设置 unlocked_at = NOW(3) 实现自动解锁。
     *
     * @param ua 用户成就记录
     * @return 受影响行数（1=插入，2=更新）
     */
    int upsert(@Param("ua") UserAchievement ua);

    /**
     * 查询用户全部成就进度（关联成就定义表获取详情 — XML 或 Service 层处理）
     */
    @Select("SELECT * FROM user_achievements " +
            "WHERE user_id = #{userId} " +
            "ORDER BY unlocked_at DESC, id ASC")
    List<UserAchievement> findByUserId(@Param("userId") Long userId);

    /**
     * 更新用户成就进度
     *
     * @param userId        用户 ID
     * @param achievementId 成就 ID
     * @param progress      当前进度
     * @return 受影响行数
     */
    @Update("UPDATE user_achievements SET progress = #{progress} " +
            "WHERE user_id = #{userId} AND achievement_id = #{achievementId}")
    int updateProgress(@Param("userId") Long userId,
                       @Param("achievementId") Long achievementId,
                       @Param("progress") int progress);
}
