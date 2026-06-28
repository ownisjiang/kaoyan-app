package com.yantiku.module.practice.mapper;

import com.yantiku.module.practice.model.entity.PracticeSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 练习会话 Mapper 接口
 *
 * insert（字段多，含 JSON config）通过 XML 实现，
 * 对应文件：mapper/practice/PracticeSessionMapper.xml
 */
@Mapper
public interface PracticeSessionMapper {

    /**
     * 根据 ID 查询练习会话
     */
    @Select("SELECT * FROM practice_sessions WHERE id = #{id}")
    PracticeSession findById(@Param("id") Long id);

    /**
     * 分页查询用户练习会话（按创建时间降序）
     */
    @Select("SELECT * FROM practice_sessions " +
            "WHERE user_id = #{userId} " +
            "ORDER BY start_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<PracticeSession> findByUserId(@Param("userId") Long userId,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit);

    /**
     * 插入练习会话 — XML 实现（字段多，含 JSON config 字段）
     *
     * @return 受影响行数
     */
    int insert(@Param("session") PracticeSession session);

    /**
     * 更新会话状态和已答题数（答题过程中实时更新）
     *
     * @param id            会话 ID
     * @param status        状态（in_progress / completed / abandoned）
     * @param answeredCount 已答题数
     * @return 受影响行数
     */
    @Update("UPDATE practice_sessions SET " +
            "  status        = #{status}, " +
            "  answered_count = #{answeredCount} " +
            "WHERE id = #{id}")
    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("answeredCount") Integer answeredCount);

    /**
     * 完成会话（写入结束时间、时长、正确数、总得分）
     *
     * @param id              会话 ID
     * @param endTime         结束时间
     * @param durationSeconds 持续时间（秒）
     * @param correctCount    正确数
     * @param totalScore      总得分
     * @return 受影响行数
     */
    @Update("UPDATE practice_sessions SET " +
            "  status            = 'completed', " +
            "  end_time          = #{endTime}, " +
            "  duration_seconds  = #{durationSeconds}, " +
            "  correct_count     = #{correctCount}, " +
            "  total_score       = #{totalScore} " +
            "WHERE id = #{id}")
    int complete(@Param("id") Long id,
                 @Param("endTime") LocalDateTime endTime,
                 @Param("durationSeconds") Integer durationSeconds,
                 @Param("correctCount") Integer correctCount,
                 @Param("totalScore") BigDecimal totalScore);

    /**
     * 更新会话得分和正确数（答题过程中实时更新）
     *
     * @param id          会话 ID
     * @param correctCount 正确数
     * @param totalScore   总得分
     * @return 受影响行数
     */
    @Update("UPDATE practice_sessions SET " +
            "  correct_count = #{correctCount}, " +
            "  total_score   = #{totalScore} " +
            "WHERE id = #{id}")
    int updateScore(@Param("id") Long id,
                     @Param("correctCount") Integer correctCount,
                     @Param("totalScore") BigDecimal totalScore);
}
