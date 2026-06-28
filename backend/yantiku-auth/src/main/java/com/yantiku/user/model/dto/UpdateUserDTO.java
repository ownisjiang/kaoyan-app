package com.yantiku.module.user.model.dto;

import lombok.Data;

/**
 * 更新用户信息DTO
 *
 * @author yantiku
 */
@Data
public class UpdateUserDTO {

    private String nickname;

    private String avatarUrl;

    private Long targetDirectionId;

    private String targetSchool;

    private Integer examYear;

    private String email;
}
