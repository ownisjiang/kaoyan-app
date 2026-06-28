package com.yantiku.module.question.mapper;

import com.yantiku.module.question.model.entity.KnowledgePoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 知识点 Mapper 接口
 *
 * findAllTree（递归 CTE 构建树形结构）通过 XML 实现，
 * 对应文件：mapper/question/KnowledgePointMapper.xml
 */
@Mapper
public interface KnowledgePointMapper {

    /**
     * 根据 ID 查询知识点
     *
     * @param id 知识点 ID
     * @return 知识点实体
     */
    @Select("SELECT * FROM knowledge_points WHERE id = #{id}")
    KnowledgePoint findById(@Param("id") Long id);

    /**
     * 根据科目 ID 查询知识点列表（按 level、sort_order 排序）
     */
    @Select("SELECT * FROM knowledge_points WHERE subject_id = #{subjectId} " +
            "ORDER BY level ASC, sort ASC, id ASC")
    List<KnowledgePoint> findBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * 查询全部知识点（树形结构） — XML 实现
     * <p>
     * 使用 MySQL 8.0 递归 CTE 构建完整树，返回扁平列表，
     * 由 Service 层组装为树形结构。
     *
     * @return 全部知识点（含层级信息，按树遍历顺序排列）
     */
    List<KnowledgePoint> findAllTree();
}
