package com.yantiku.module.achievement.mapper;

import com.yantiku.module.achievement.model.entity.Achievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 成就定义 Mapper 接口
 */
@Mapper
public interface AchievementMapper {

    /**
     * 查询全部成就（按 sort_order 排序）
     */
    @Select("SELECT * FROM achievements ORDER BY sort_order ASC, id ASC")
    List<Achievement> findAll();

    /**
     * 根据成就编码查询
     */
    @Select("SELECT * FROM achievements WHERE code = #{code}")
    Achievement findByCode(@Param("code") String code);

    /**
     * 根据分类查询成就列表
     *
     * @param category 成就分类（streak / answer / accuracy / etc）
     * @return 成就列表
     */
    @Select("SELECT * FROM achievements WHERE category = #{category} ORDER BY sort_order ASC, id ASC")
    List<Achievement> findByCategory(@Param("category") String category);
}
