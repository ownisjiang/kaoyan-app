package com.yantiku.module.question.mapper;

import com.yantiku.module.question.model.dto.QuestionSearchDTO;
import com.yantiku.module.question.model.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 题目 Mapper 接口
 *
 * 复杂查询（searchQuestions / countQuestions / findRandom / insert / update）
 * 通过 XML 实现，对应文件：mapper/question/QuestionMapper.xml
 *
 * 注意：questions 表的 correct_rate 列为 GENERATED ALWAYS AS，
 * INSERT/UPDATE 中不应包含该列。
 */
@Mapper
public interface QuestionMapper {

    /**
     * 根据 ID 查询题目
     */
    @Select("SELECT id, type AS questionType, difficulty, " +
            "subject_id AS subjectId, knowledge_point_id AS knowledgePointId, " +
            "content, options, answer, explanation AS analysis, source, " +
            "year AS examYear, use_count AS useCount, correct_count AS correctCount, " +
            "is_active AS isActive, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM questions WHERE id = #{id}")
    Question findById(@Param("id") Long id);

    // ========================================================
    // 分页筛选搜索 — XML 实现（动态 WHERE + 分页）
    // ========================================================

    /**
     * 分页查询题目列表（支持多条件筛选）
     *
     * @param dto 搜索条件 DTO（directionId, subjectId, knowledgePointId,
     *            questionType, difficulty, examYear, keyword, offset, limit）
     * @return 题目列表
     */
    List<Question> searchQuestions(@Param("dto") QuestionSearchDTO dto);

    /**
     * 统计符合条件的题目总数（用于分页计算）
     *
     * @param dto 搜索条件 DTO（同上，忽略 offset/limit）
     * @return 总记录数
     */
    long countQuestions(@Param("dto") QuestionSearchDTO dto);

    // ========================================================
    // 随机 / 热门题目
    // ========================================================

    /**
     * 随机获取题目 — XML 实现（动态 WHERE + ORDER BY RAND()）
     *
     * @param subjectId    科目 ID（可选）
     * @param questionType 题目类型（可选：1=选择题 2=填空题 3=综合题）
     * @param limit        返回数量
     * @return 随机题目列表
     */
    List<Question> findRandom(@Param("subjectId") Long subjectId,
                              @Param("questionType") Integer questionType,
                              @Param("limit") int limit);

    /**
     * 查询热门题目（按使用次数降序）
     */
    @Select("SELECT id, type AS questionType, difficulty, " +
            "subject_id AS subjectId, knowledge_point_id AS knowledgePointId, " +
            "content, options, answer, explanation AS analysis, source, " +
            "year AS examYear, use_count AS useCount, correct_count AS correctCount, " +
            "is_active AS isActive, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM questions WHERE is_active = 1 " +
            "ORDER BY use_count DESC, id ASC LIMIT #{limit}")
    List<Question> findHot(@Param("limit") int limit);

    // ========================================================
    // 写操作
    // ========================================================

    /**
     * 插入题目 — XML 实现（字段多，跳过 generated 列 correct_rate）
     *
     * @return 受影响行数
     */
    int insert(@Param("question") Question question);

    /**
     * 更新题目 — XML 实现（动态 SET，跳过 generated 列 correct_rate）
     *
     * @return 受影响行数
     */
    int update(@Param("question") Question question);

    /**
     * 原子递增题目使用统计
     *
     * @param id        题目 ID
     * @param isCorrect 本次答题是否正确
     * @return 受影响行数
     */
    @Update("UPDATE questions SET " +
            "  use_count     = use_count + 1, " +
            "  correct_count = correct_count + IF(#{isCorrect}, 1, 0), " +
            "  updated_at    = NOW(3) " +
            "WHERE id = #{id}")
    int incrementUseCount(@Param("id") Long id, @Param("isCorrect") boolean isCorrect);

    /**
     * 分页查询某科目下指定题型的题目（非随机，按ID升序）
     * 当 subjectId 为 null 时查询所有科目
     */
    @Select("<script>" +
            "SELECT id, type AS questionType, difficulty, " +
            "subject_id AS subjectId, knowledge_point_id AS knowledgePointId, " +
            "content, options, answer, explanation AS analysis, source, " +
            "year AS examYear, use_count AS useCount, correct_count AS correctCount, " +
            "is_active AS isActive, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM questions WHERE is_active = 1 " +
            "<if test='subjectId != null'>AND subject_id = #{subjectId}</if> " +
            "<if test='questionType != null'>AND type = #{questionType}</if> " +
            "ORDER BY type ASC, id ASC LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Question> findPagedBySubject(@Param("subjectId") Long subjectId,
                                       @Param("questionType") Integer questionType,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit);

    // ========================================================
    // 统计查询 — 用于科目概览页
    // ========================================================

    /**
     * 按科目分组统计各题型数量
     *
     * @return List<Map> 每项包含 subject_id, type, cnt
     */
    @Select("SELECT subject_id, type, COUNT(*) AS cnt " +
            "FROM questions " +
            "WHERE is_active = 1 " +
            "GROUP BY subject_id, type " +
            "ORDER BY subject_id, type")
    List<Map<String, Object>> countGroupBySubjectAndType();

    /**
     * 统计某个科目下活跃题目总数
     */
    @Select("SELECT COUNT(*) FROM questions WHERE subject_id = #{subjectId} AND is_active = 1")
    int countActiveBySubject(@Param("subjectId") Long subjectId);

    /**
     * 按题型统计某科目题目数（返回 type + cnt）
     */
    @Select("SELECT type, COUNT(*) AS cnt FROM questions " +
            "WHERE subject_id = #{subjectId} AND is_active = 1 " +
            "GROUP BY type ORDER BY type")
    List<Map<String, Object>> countBySubjectAndType(@Param("subjectId") Long subjectId);
}
