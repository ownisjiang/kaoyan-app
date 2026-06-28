package com.yantiku.module.auth.mapper;

import com.yantiku.module.auth.model.entity.RefreshToken;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 刷新令牌 Mapper 接口
 * <p>
 * 用于 JWT Refresh Token 轮换、族撤销、过期清理等操作。
 */
@Mapper
public interface RefreshTokenMapper {

    /**
     * 根据令牌哈希查询令牌（用于验证 refresh token）
     */
    @Select("SELECT * FROM refresh_tokens WHERE token_hash = #{tokenHash}")
    RefreshToken findByTokenHash(@Param("tokenHash") String tokenHash);

    /**
     * 插入刷新令牌
     */
    @Insert("INSERT INTO refresh_tokens " +
            "(user_id, token_hash, family, device_info, ip_address, expires_at, created_at) " +
            "VALUES " +
            "(#{token.userId}, #{token.tokenHash}, #{token.family}, " +
            " #{token.deviceInfo}, #{token.ipAddress}, #{token.expiresAt}, NOW(3))")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(@Param("token") RefreshToken token);

    /**
     * 撤销整个令牌族（检测到令牌复用攻击时调用，使该族所有令牌失效）
     *
     * @param family 令牌族标识
     * @return 受影响行数
     */
    @Update("UPDATE refresh_tokens SET revoked_at = NOW(3) " +
            "WHERE family = #{family} AND revoked_at IS NULL")
    int revokeByFamily(@Param("family") String family);

    /**
     * 撤销单个令牌（正常轮换时撤销旧令牌）
     *
     * @param id 令牌 ID
     * @return 受影响行数
     */
    @Update("UPDATE refresh_tokens SET revoked_at = NOW(3) " +
            "WHERE id = #{id} AND revoked_at IS NULL")
    int revokeToken(@Param("id") Long id);

    /**
     * 删除已过期的令牌（定时清理任务调用）
     *
     * @return 删除行数
     */
    @Delete("DELETE FROM refresh_tokens WHERE expires_at < NOW(3)")
    int deleteExpired();
}
