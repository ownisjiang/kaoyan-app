package com.yantiku.module.question.mapper;

import com.yantiku.module.question.model.entity.Direction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 考试方向 Mapper 接口
 */
@Mapper
public interface DirectionMapper {

    /**
     * 查询全部方向（按 sort_order 排序）
     */
    @Select("SELECT * FROM directions ORDER BY sort ASC, id ASC")
    List<Direction> findAll();

    /**
     * 根据 ID 查询方向
     */
    @Select("SELECT * FROM directions WHERE id = #{id}")
    Direction findById(@Param("id") Long id);

    /**
     * 查询所有启用的方向
     */
    @Select("SELECT * FROM directions WHERE is_active = 1 ORDER BY sort ASC, id ASC")
    List<Direction> findActive();
}
