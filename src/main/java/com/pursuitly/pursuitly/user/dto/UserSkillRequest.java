package com.pursuitly.pursuitly.user.dto;

import com.pursuitly.pursuitly.common.enums.Proficiency;
import com.pursuitly.pursuitly.common.enums.SkillCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSkillRequest {
    private String skillName;
    private SkillCategory category;
    private Proficiency proficiency;
    private Double yearsExperience;
}
