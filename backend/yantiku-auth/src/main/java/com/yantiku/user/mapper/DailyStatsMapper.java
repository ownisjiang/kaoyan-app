package com.yantiku.module.user.mapper;

import com.yantiku.module.user.model.entity.DailyStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户每日统计 Mapper 接口
 *
 * upsert（ON DUPLICATE KEY UPDATE）通过 XML 实现，
 * 对应文件：mapper/user/DailyStatsMapper.xml
 */
@Mapper
public interface DailyStatsMapper {

    /**
     * 根据用户名和日期查询统计
     */
    @Select("SELECT id, user_id AS userId, stat_date AS statDate, " +
            "questions_answered AS questionsAnswered, correct_count AS correctCount, " +
            "duration_sec AS studyDurationSeconds, " +
            "sessions_count AS sessionsCount, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM daily_stats WHERE user_id = #{userId} AND stat_date = #{statDate}")
    DailyStats findByUserIdAndDate(@Param("userId") Long userId, 
                                   @Param("statDate") LocalDate statDate);
    
    /**
     * 插入或更新当日统计 — XML 实现
     * <p>
     * 利用 UNIQUE KEY uk_user_date (user_id, stat_date) 触发
     * ON DUPLICATE KEY UPDATE：
     * <ul>
     *   <li>questions_answered += 1</li>
     *   <li>correct_count += isCorrect（0 或 1）</li>
     *   <li>updated_at = NOW(3)</li>
     * </ul>
     *
     * @param userId    用户 ID
     * @param isCorrect 是否正确（1=正确，0=错误）
     * @return 受影响行数（1=插入，2=更新）
     */
    int upsert(@Param("userId") Long userId, @Param("isCorrect") int isCorrect);

    /**
     * 查询指定日期范围内的每日统计
     */
    @Select("SELECT id, user_id AS userId, stat_date AS statDate, " +
            "questions_answered AS questionsAnswered, correct_count AS correctCount, " +
            "duration_sec AS studyDurationSeconds, " +
            "sessions_count AS sessionsCount, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM daily_stats " +
            "WHERE user_id = #{userId} " +
            "  AND stat_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY stat_date ASC")
    List<DailyStats> findByUserId(@Param("userId") Long userId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);
                                  
    /**
     * 递增答题数
     */
    @Update("UPDATE daily_stats SET " +
            "  questions_answered = questions_answered + #{increment}, " +
            "  updated_at = NOW(3) " +
            "WHERE id = #{id}")
    int incrementQuestionsAnswered(@Param("id") Long id, 
                                   @Param("increment") int increment);
    
    /**
     * 递增正确数
     */
    @Update("UPDATE daily_stats SET " +
            "  correct_count = correct_count + #{increment}, " +
            "  updated_at = NOW(3) " +
            "WHERE id = #{id}")
    int incrementCorrectCount(@Param("id") Long id, 
                              @Param("increment") int increment);
    
    /**
     * 递增学习时长
     */
    @Update("UPDATE daily_stats SET " +
            "  duration_sec = duration_sec + #{increment}, " +
            "  updated_at = NOW(3) " +
            "WHERE id = #{id}")
    int incrementStudyDuration(@Param("id") Long id, 
                               @Param("increment") int increment);
                               
    /**
     * 插入新记录
     */
    int insert(@Param("stats") DailyStats stats);

    /**
     * 查询用户活跃日期列表（按日期降序，最多365天），用于计算连续天数
     * XML 实现: mapper/user/DailyStatsMapper.xml
     */
    List<java.time.LocalDate> countConsecutiveDays(@Param("userId") Long userId,
                                                    @Param("today") LocalDate today);
}
