package com.yantiku.module.wrongbook.mapper;

import com.yantiku.module.wrongbook.model.entity.WrongQuestionBook;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 错题本 Mapper 接口
 *
 * upsert（ON DUPLICATE KEY UPDATE）和 batchUpdateStatus（foreach 批量更新）
 * 通过 XML 实现，对应文件：mapper/wrongbook/WrongQuestionBookMapper.xml
 */
@Mapper
public interface WrongQuestionBookMapper {

    /**
     * 插入或更新错题记录 — XML 实现
     * <p>
     * 利用 UNIQUE KEY uk_user_question (user_id, question_id) 触发
     * ON DUPLICATE KEY UPDATE：
     * <ul>
     *   <li>wrong_count += 1</li>
     *   <li>consecutive_correct = 0（重置连续正确数）</li>
     *   <li>last_wrong_at = NOW(3)</li>
     *   <li>status = 'active'（重新激活已归档的错题）</li>
     * </ul>
     *
     * @param entry 错题记录（userId, questionId, knowledgePointId 必填）
     * @return 受影响行数（1=插入，2=更新）
     */
    int upsert(@Param("entry") WrongQuestionBook entry);

    /**
     * 根据记录 ID 查询错题（用于复习操作前获取掌握等级等信息）
     *
     * @param id 错题记录 ID
     * @return 错题记录
     */
    @Select("SELECT * FROM wrong_question_book WHERE id = #{id}")
    WrongQuestionBook findById(@Param("id") Long id);

    /**
     * 游标分页查询用户错题（基于 ID 倒序游标，适合无限滚动）
     *
     * @param userId 用户 ID
     * @param cursor 游标（上一页最后一条记录的 ID，首页传 null）
     * @param size   每页数量
     * @return 错题列表
     */
    @Select("SELECT * FROM wrong_question_book " +
            "WHERE user_id = #{userId} " +
            "  AND status = 'active' " +
            "  AND (#{cursor} IS NULL OR id < #{cursor}) " +
            "ORDER BY id DESC " +
            "LIMIT #{size}")
    List<WrongQuestionBook> findByUserIdCursor(@Param("userId") Long userId,
                                                @Param("cursor") Long cursor,
                                                @Param("size") int size);

    /**
     * 查询用户全部错题（不分页，用于导出或批量处理）
     */
    @Select("SELECT * FROM wrong_question_book " +
            "WHERE user_id = #{userId} AND status = 'active' " +
            "ORDER BY id DESC")
    List<WrongQuestionBook> findByUserId(@Param("userId") Long userId);

    /**
     * 更新复习记录（间隔重复算法计算下次复习时间）
     *
     * @param id            错题记录 ID
     * @param reviewedAt    本次复习时间
     * @param nextReviewAt  下次复习时间
     * @return 受影响行数
     */
    @Update("UPDATE wrong_question_book SET " +
            "  last_reviewed_at = #{reviewedAt}, " +
            "  next_review_at   = #{nextReviewAt}, " +
            "  updated_at       = NOW(3) " +
            "WHERE id = #{id}")
    int updateReview(@Param("id") Long id,
                     @Param("reviewedAt") LocalDateTime reviewedAt,
                     @Param("nextReviewAt") LocalDateTime nextReviewAt);

    /**
     * 更新单条错题状态
     *
     * @param id     错题记录 ID
     * @param status 状态（active / archived / mastered）
     * @return 受影响行数
     */
    @Update("UPDATE wrong_question_book SET " +
            "  status = #{status}, updated_at = NOW(3) " +
            "WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 批量更新错题状态 — XML 实现（foreach + IN 子句）
     *
     * @param ids    错题记录 ID 列表
     * @param status 目标状态
     * @return 受影响行数
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") String status);

    /**
     * 查询用户错题统计（总数 + 待复习数）
     * <p>
     * 待复习定义：status = 'active' 且
     * (next_review_at IS NULL 或 next_review_at &lt;= NOW(3))
     *
     * @param userId 用户 ID
     * @return 统计结果
     */
    @Select("SELECT " +
            "  COUNT(*) AS total_wrong, " +
            "  SUM(CASE WHEN status = 'active' " +
            "           AND (next_review_at IS NULL OR next_review_at <= NOW(3)) " +
            "      THEN 1 ELSE 0 END) AS need_review " +
            "FROM wrong_question_book " +
            "WHERE user_id = #{userId} AND status != 'archived'")
    WrongQuestionStats selectStats(@Param("userId") Long userId);

    /**
     * 根据用户ID和题目ID查询错题记录
     *
     * @param userId    用户 ID
     * @param questionId 题目 ID
     * @return 错题记录
     */
    @Select("SELECT * FROM wrong_question_book " +
            "WHERE user_id = #{userId} AND question_id = #{questionId}")
    WrongQuestionBook findByUserIdAndQuestionId(@Param("userId") Long userId, 
                                                 @Param("questionId") Long questionId);

    /**
     * 递增错误次数
     *
     * @param id 错题记录 ID
     * @return 受影响行数
     */
    @Update("UPDATE wrong_question_book SET " +
            "  wrong_count = wrong_count + 1, " +
            "  consecutive_correct = 0, " +
            "  last_wrong_at = NOW(3), " +
            "  status = 'active', " +
            "  updated_at = NOW(3) " +
            "WHERE id = #{id}")
    int incrementWrongCount(@Param("id") Long id);

    /**
     * 递增连续正确次数
     *
     * @param id 错题记录 ID
     * @return 受影响行数
     */
    @Update("UPDATE wrong_question_book SET " +
            "  consecutive_correct = consecutive_correct + 1, " +
            "  updated_at = NOW(3) " +
            "WHERE id = #{id}")
    int incrementConsecutiveCorrect(@Param("id") Long id);

    // ========================================================
    // 错题统计内部类
    // ========================================================

    /**
     * 错题统计数据结构
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WrongQuestionStats {
        /** 错题总数（不含已归档） */
        private int totalWrong;
        /** 待复习数（status=active 且到期） */
        private int needReview;
    }
}
