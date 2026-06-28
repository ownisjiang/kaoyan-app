package com.yantiku.module.practice.mapper;

import com.yantiku.module.practice.model.entity.UserAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户答题记录 Mapper 接口
 *
 * insert（字段多，含 JSON 字段）通过 XML 实现，
 * 对应文件：mapper/practice/UserAnswerMapper.xml
 */
@Mapper
public interface UserAnswerMapper {

    /**
     * 插入答题记录 — XML 实现（字段多，含 JSON user_answer / ai_feedback）
     *
     * @return 受影响行数
     */
    int insert(@Param("answer") UserAnswer answer);

    /**
     * 根据会话 ID 查询答题记录（按提交时间升序）
     */
    @Select("SELECT * FROM user_answers " +
            "WHERE session_id = #{sessionId} " +
            "ORDER BY submitted_at ASC, id ASC")
    List<UserAnswer> findBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 分页查询用户答题记录（按提交时间降序）
     */
    @Select("SELECT * FROM user_answers " +
            "WHERE user_id = #{userId} " +
            "ORDER BY submitted_at DESC " +
            "LIMIT #{offset}, #{limit}")
    List<UserAnswer> findByUserId(@Param("userId") Long userId,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    /**
     * 更新 AI 反馈和得分（异步 AI 评判完成后回调）
     *
     * @param id         答题记录 ID
     * @param aiFeedback AI 反馈（JSON 字符串）
     * @param score      得分
     * @return 受影响行数
     */
    @Update("UPDATE user_answers SET " +
            "  ai_feedback = #{aiFeedback}, " +
            "  score       = #{score} " +
            "WHERE id = #{id}")
    int updateFeedback(@Param("id") Long id,
                       @Param("aiFeedback") String aiFeedback,
                       @Param("score") BigDecimal score);

    /**
     * 统计用户在每个科目下的正确答题数（去重题目）
     *
     * @param userId 用户 ID
     * @return List<Map> 每项包含 subject_id, correct_cnt
     */
    @Select("SELECT q.subject_id, COUNT(DISTINCT ua.question_id) AS correct_cnt " +
            "FROM user_answers ua " +
            "JOIN questions q ON ua.question_id = q.id " +
            "WHERE ua.user_id = #{userId} AND ua.is_correct = 1 AND q.is_active = 1 " +
            "GROUP BY q.subject_id")
    List<Map<String, Object>> countCorrectBySubject(@Param("userId") Long userId);

    /**
     * 统计用户在每个科目下的总答题数（去重题目）
     *
     * @param userId 用户 ID
     * @return List<Map> 每项包含 subject_id, total_cnt
     */
    @Select("SELECT q.subject_id, COUNT(DISTINCT ua.question_id) AS total_cnt " +
            "FROM user_answers ua " +
            "JOIN questions q ON ua.question_id = q.id " +
            "WHERE ua.user_id = #{userId} AND q.is_active = 1 " +
            "GROUP BY q.subject_id")
    List<Map<String, Object>> countTotalBySubject(@Param("userId") Long userId);
}
