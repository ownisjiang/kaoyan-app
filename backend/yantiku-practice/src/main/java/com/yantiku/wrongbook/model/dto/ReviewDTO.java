package com.yantiku.module.wrongbook.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 错题复习请求 DTO
 */
@Data
public class ReviewDTO {

    /** 错题记录ID */
    @NotNull(message = "错题记录ID不能为空")
    private Long wrongBookId;
}
