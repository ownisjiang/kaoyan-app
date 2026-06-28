package com.yantiku.module.auth.model.vo;

import com.yantiku.module.user.model.vo.UserVO;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * 认证视图对象
 *
 * @author yantiku
 */
@Data
@Builder
public class AuthVO {

    /** 访问令牌 */
    private String accessToken;

    /** 刷新令牌 */
    private String refreshToken;

    /** 令牌类型 */
    @Default
    private String tokenType = "Bearer";

    /** 过期时间（秒） */
    private long expiresIn;

    /** 用户信息 */
    private UserVO user;
}
