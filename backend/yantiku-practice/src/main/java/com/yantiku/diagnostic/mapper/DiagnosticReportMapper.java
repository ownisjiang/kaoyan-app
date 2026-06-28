package com.yantiku.module.diagnostic.mapper;

import com.yantiku.module.diagnostic.model.entity.DiagnosticReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 诊断报告 Mapper 接口
 *
 * insert（字段多，含多个 JSON 字段）通过 XML 实现，
 * 对应文件：mapper/diagnostic/DiagnosticReportMapper.xml
 */
@Mapper
public interface DiagnosticReportMapper {

    /**
     * 插入诊断报告 — XML 实现（字段多，含 JSON 字段 radar_data / weak_points /
     * strengths / suggestions / time_analysis）
     *
     * @return 受影响行数
     */
    int insert(@Param("report") DiagnosticReport report);

    /**
     * 根据 ID 查询诊断报告
     */
    @Select("SELECT * FROM diagnostic_reports WHERE id = #{id}")
    DiagnosticReport findById(@Param("id") Long id);

    /**
     * 分页查询用户诊断报告（按生成时间降序）
     */
    @Select("SELECT * FROM diagnostic_reports " +
            "WHERE user_id = #{userId} " +
            "ORDER BY generated_at DESC " +
            "LIMIT #{offset}, #{limit}")
    List<DiagnosticReport> findByUserId(@Param("userId") Long userId,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    /**
     * 统计用户诊断报告总数
     */
    @Select("SELECT COUNT(*) FROM diagnostic_reports WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);
}
