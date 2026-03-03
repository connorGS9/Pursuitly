package com.pursuitly.pursuitly.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserExperienceRequest {
    private String company;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean current;
    private List<String> bullets;
}