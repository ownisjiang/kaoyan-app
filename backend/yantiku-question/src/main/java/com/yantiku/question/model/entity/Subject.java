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
public class Subject {
    private Long id;
    private Long directionId;
    private String name;
    private String code;
    private Integer sort;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
