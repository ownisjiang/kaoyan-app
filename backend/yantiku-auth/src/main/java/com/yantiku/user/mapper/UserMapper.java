package com.yantiku.module.user.mapper;

import com.yantiku.module.user.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户 Mapper 接口
 *
 * 简单查询使用注解内联 SQL，复杂查询（insert/update 含大量字段、动态 SET）
 * 通过 XML 实现，对应文件：mapper/user/UserMapper.xml
 */
@Mapper
public interface UserMapper {

    /**
     * 根据 ID 查询用户（排除软删除）
     */
    @Select("SELECT * FROM users WHERE id = #{id} AND deleted_at IS NULL")
    User findById(@Param("id") Long id);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM users WHERE phone = #{phone} AND deleted_at IS NULL")
    User findByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND deleted_at IS NULL")
    User findByEmail(@Param("email") String email);

    /**
     * 插入用户 — XML 实现（字段多，含可选字段）
     *
     * @return 受影响行数
     */
    int insert(@Param("user") User user);

    /**
     * 更新用户信息 — XML 实现（动态 SET，仅更新非空字段）
     *
     * @return 受影响行数
     */
    int update(@Param("user") User user);

    /**
     * 软删除用户（设置 deleted_at）
     */
    @Update("UPDATE users SET deleted_at = NOW(3), updated_at = NOW(3) " +
            "WHERE id = #{id} AND deleted_at IS NULL")
    int softDelete(@Param("id") Long id);

    /**
     * 更新用户答题统计（原子自增）
     *
     * @param id          用户 ID
     * @param isCorrect   本次答题是否正确
     * @param durationSec 本次答题耗时（秒）
     */
    @Update("UPDATE users SET " +
            "  total_questions   = total_questions + 1, " +
            "  total_correct     = total_correct + IF(#{isCorrect}, 1, 0), " +
            "  total_duration_sec = total_duration_sec + #{durationSec}, " +
            "  last_active_date  = CURDATE(), " +
            "  updated_at        = NOW(3) " +
            "WHERE id = #{id} AND deleted_at IS NULL")
    int updateStats(@Param("id") Long id,
                    @Param("isCorrect") boolean isCorrect,
                    @Param("durationSec") long durationSec);

    /**
     * 批量更新用户累积统计（完成会话时调用）
     */
    @Update("UPDATE users SET " +
            "  total_questions    = #{totalQuestions}, " +
            "  total_correct      = #{totalCorrect}, " +
            "  total_duration_sec = #{totalDurationSec}, " +
            "  last_active_date   = CURDATE(), " +
            "  updated_at         = NOW(3) " +
            "WHERE id = #{id}")
    int updateCumulativeStats(@Param("id") Long id,
                              @Param("totalQuestions") int totalQuestions,
                              @Param("totalCorrect") int totalCorrect,
                              @Param("totalDurationSec") int totalDurationSec);

    /**
     * 更新连续学习天数
     */
    @Update("UPDATE users SET streak_days = #{streak}, updated_at = NOW(3) " +
            "WHERE id = #{id}")
    int updateStreak(@Param("id") Long id, @Param("streak") int streak);
}
