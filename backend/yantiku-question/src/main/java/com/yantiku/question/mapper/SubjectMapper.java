package com.yantiku.module.question.mapper;

import com.yantiku.module.question.model.entity.Subject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 科目 Mapper 接口
 */
@Mapper
public interface SubjectMapper {

    /**
     * 根据方向 ID 查询科目列表（按 sort_order 排序）
     */
    @Select("SELECT * FROM subjects WHERE direction_id = #{directionId} ORDER BY sort ASC, id ASC")
    List<Subject> findByDirectionId(@Param("directionId") Long directionId);

    @Select("SELECT * FROM subjects ORDER BY sort ASC, id ASC")
    List<Subject> findAll();

    /**
     * 根据 ID 查询科目
     */
    @Select("SELECT * FROM subjects WHERE id = #{id}")
    Subject findById(@Param("id") Long id);
}
