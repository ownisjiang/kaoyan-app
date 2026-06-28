package com.yantiku.module.auth.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新Token DTO
 *
 * @author yantiku
 */
@Data
public class RefreshTokenDTO {

    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}
