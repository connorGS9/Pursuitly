package com.pursuitly.pursuitly.user.dto;

import com.pursuitly.pursuitly.common.enums.Proficiency;
import com.pursuitly.pursuitly.common.enums.SkillCategory;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserSkillResponse {
    private UUID id;
    private String skillName;
    private SkillCategory category;
    private Proficiency proficiency;
    private Double yearsExperience;
}