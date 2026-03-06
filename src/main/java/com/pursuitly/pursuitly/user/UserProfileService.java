package com.pursuitly.pursuitly.user;

import com.pursuitly.pursuitly.embedding.EmbeddingService;
import com.pursuitly.pursuitly.jobs.JobMatchController;
import com.pursuitly.pursuitly.jobs.JobMatchService;
import com.pursuitly.pursuitly.user.dto.*;
import com.pursuitly.pursuitly.user.model.User;
import com.pursuitly.pursuitly.user.model.UserExperience;
import com.pursuitly.pursuitly.user.model.UserProfile;
import com.pursuitly.pursuitly.user.model.UserSkill;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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
    private final JobMatchService jobMatchService;

    public UserProfile createOrUpdateProfile(UUID userId, UserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(UserProfile.builder().user(user).build());

        profile.setDesiredTitle(request.getDesiredTitle());
        profile.setExperienceLevel(request.getExperienceLevel());
        profile.setLocation(request.getLocation());
        profile.setRemotePreference(request.getRemotePreference());
        profile.setSalaryMin(request.getSalaryMin());
        profile.setSummary(request.getSummary());

        UserProfile saved = userProfileRepository.save(profile);
        jobMatchService.updateUserEmbedding();
        return saved;
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

    @Transactional
    public List<UserSkill> bulkUpdateSkills(UUID userId, List<UserSkillRequest> skillRequests) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        userSkillRepository.deleteAllByUserId(userId);

        List<UserSkill> skills = skillRequests.stream()
                .map(req -> UserSkill.builder()
                        .user(user)
                        .skillName(req.getSkillName())
                        .category(req.getCategory())
                        .proficiency(req.getProficiency())
                        .yearsExperience(req.getYearsExperience())
                        .build())
                .toList();

        List<UserSkill> saved = userSkillRepository.saveAll(skills);
        return saved;
    }
    @Transactional
    public List<UserExperience> bulkUpdateExperience(UUID userId, List<UserExperienceRequest> experienceRequests) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        userExperienceRepository.deleteAllByUserId(userId);

        List<UserExperience> experiences = experienceRequests.stream()
                .map(req -> UserExperience.builder()
                        .user(user)
                        .company(req.getCompany())
                        .title(req.getTitle())
                        .startDate(req.getStartDate())
                        .endDate(req.getEndDate())
                        .current(req.isCurrent())
                        .bullets(req.getBullets())
                        .build())
                .toList();

        List<UserExperience> saved = userExperienceRepository.saveAll(experiences);
        return saved;
    }

    @Transactional
    public List<UserExperience> getExperience(UUID userId) {
        return userExperienceRepository.findAllByUserIdOrderByStartDateDesc(userId);
    }

    public void deleteExperience(UUID experienceId, UUID userId) {
        userExperienceRepository.deleteByIdAndUserId(experienceId, userId);
    }
}
