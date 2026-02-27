package com.pursuitly.pursuitly.user.model;

import com.pursuitly.pursuitly.user.model.enums.Proficiency;
import com.pursuitly.pursuitly.user.model.enums.SkillCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "user_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String skillName;

    @Enumerated(EnumType.STRING)
    private SkillCategory category;

    @Enumerated(EnumType.STRING)
    private Proficiency proficiency;

    private Double yearsExperience;
}
