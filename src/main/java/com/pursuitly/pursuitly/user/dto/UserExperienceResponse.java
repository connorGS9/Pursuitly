package com.pursuitly.pursuitly.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserExperienceResponse {
    private UUID id;
    private String company;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> bullets;
    private boolean current;
}
