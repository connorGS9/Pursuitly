package com.pursuitly.pursuitly.user;

import com.pursuitly.pursuitly.user.dto.UserExperienceResponse;
import com.pursuitly.pursuitly.user.dto.UserProfileResponse;
import com.pursuitly.pursuitly.user.dto.UserSkillResponse;
import com.pursuitly.pursuitly.user.model.User;
import com.pursuitly.pursuitly.user.model.UserExperience;
import com.pursuitly.pursuitly.user.model.UserProfile;
import com.pursuitly.pursuitly.user.model.UserSkill;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserExperienceRepository userExperienceRepository;
    private final UserRepository userRepository;

    public UserProfile createOrUpdateProfile(UUID userId, UserProfile profileData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(UserProfile.builder().user(user).build());

        profile.setDesiredTitle(profileData.getDesiredTitle());
        profile.setExperienceLevel(profileData.getExperienceLevel());
        profile.setLocation(profileData.getLocation());
        profile.setRemotePreference(profileData.getRemotePreference());
        profile.setSalaryMin(profileData.getSalaryMin());
        profile.setSummary(profileData.getSummary());

        return userProfileRepository.save(profile);
    }

    // Build a DTO to return user profile to client that doesn't include hashed password, etc.
    public UserProfileResponse toResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .id(profile.getId())
                .email(profile.getUser().getEmail())
                .fullName(profile.getUser().getFullName())
                .desiredTitle(profile.getDesiredTitle())
                .experienceLevel(profile.getExperienceLevel())
                .location(profile.getLocation())
                .remotePreference(profile.getRemotePreference())
                .salaryMin(profile.getSalaryMin())
                .summary(profile.getSummary())
                .build();
    }

    // Create a DTO to return skills of a user without nested user info included
    public UserSkillResponse toSkillResponse(UserSkill skill) {
        return UserSkillResponse.builder()
                .id(skill.getId())
                .skillName(skill.getSkillName())
                .category(skill.getCategory())
                .proficiency(skill.getProficiency())
                .yearsExperience(skill.getYearsExperience())
                .build();
    }
    // Create a DTO for user experience response that only returns experience not user data, etc.
    public UserExperienceResponse toExperienceResponse(UserExperience experience) {
        return UserExperienceResponse.builder()
                .id(experience.getId())
                .company(experience.getCompany())
                .title(experience.getTitle())
                .startDate(experience.getStartDate())
                .endDate(experience.getEndDate())
                .bullets(experience.getBullets())
                .current(experience.isCurrent())
                .build();
    }
    public UserProfile getProfile(UUID userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));
    }

    public UserSkill addSkill(UUID userId, UserSkill skill) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        skill.setUser(user);
        return userSkillRepository.save(skill);
    }

    public List<UserSkill> getSkills(UUID userId) {
        return userSkillRepository.findAllByUserId(userId);
    }

    public void deleteSkill(UUID skillId, UUID userId) {
        userSkillRepository.deleteByIdAndUserId(skillId, userId);
    }

    public UserExperience addExperience(UUID userId, UserExperience experience) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        experience.setUser(user);
        return userExperienceRepository.save(experience);
    }

    public List<UserExperience> getExperience(UUID userId) {
        return userExperienceRepository.findAllByUserIdOrderByStartDateDesc(userId);
    }

    public void deleteExperience(UUID experienceId, UUID userId) {
        userExperienceRepository.deleteByIdAndUserId(experienceId, userId);
    }
}
