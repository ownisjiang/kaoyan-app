package com.yantiku.module.question.model.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Direction {
    private Long id;
    private String code;
    private String name;
    private Integer sort;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
