package com.pursuitly.pursuitly.user;

import com.pursuitly.pursuitly.user.dto.UserExperienceResponse;
import com.pursuitly.pursuitly.user.dto.UserProfileResponse;
import com.pursuitly.pursuitly.user.dto.UserSkillResponse;
import com.pursuitly.pursuitly.user.model.UserExperience;
import com.pursuitly.pursuitly.user.model.UserProfile;
import com.pursuitly.pursuitly.user.model.UserSkill;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;
    private final UserRepository userRepository;

    /*
        Principal principal — Spring automatically injects this into any controller method and it holds the currently authenticated user's email
         extracted from the JWT token. So instead of passing a userId in the request body
         (which would be a security risk since anyone could fake it),
        we always derive the userId server side from the token using getCurrentUserId().
    */
    private UUID getCurrentUserId(Principal principal) { //Get the user profile of the authenticated user from the principal
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    @GetMapping //Get profile of user from passed principal, return dto to client
    public ResponseEntity<UserProfileResponse> getProfile(Principal principal) {
       UserProfile profile = userProfileService.getProfile(getCurrentUserId(principal));
       return ResponseEntity.ok(userProfileService.toResponse(profile));
    }

    @PostMapping //Update or create a profile for user from request body + principal of user, return dto to client
    public ResponseEntity<UserProfileResponse> createOrUpdateProfile(@RequestBody UserProfile profile, Principal principal) {
        UserProfile saved = userProfileService.createOrUpdateProfile(getCurrentUserId(principal), profile);
        return ResponseEntity.ok(userProfileService.toResponse(saved));
    }

    @PostMapping("/skills") //Add a skill to user from principal authentication
    public ResponseEntity<UserSkillResponse> addSkill(@RequestBody UserSkill skill, Principal principal) {
        UserSkill saved = userProfileService.addSkill(getCurrentUserId(principal), skill);
        return ResponseEntity.ok(userProfileService.toSkillResponse(saved));
    }

    @GetMapping("/skills") //Get list of skills for this authenticated user
    public ResponseEntity<List<UserSkillResponse>> getSkills(Principal principal) {
        return ResponseEntity.ok(userProfileService.getSkills(getCurrentUserId(principal))
                .stream()
                .map(userProfileService::toSkillResponse)
                .toList());
    }

    @DeleteMapping("/skills/{skillId}") //Delete a skill (skillID) from authenticated user's list of skills
    public ResponseEntity<Void> deleteSkill(@PathVariable UUID skillId, Principal principal) {
        userProfileService.deleteSkill(skillId, getCurrentUserId(principal));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/experience") //Add job experience for this authenticated user
    public ResponseEntity<UserExperienceResponse> addExperience(@RequestBody UserExperience experience, Principal principal) {
        UserExperience saved = userProfileService.addExperience(getCurrentUserId(principal), experience);
        return ResponseEntity.ok(userProfileService.toExperienceResponse(saved));
    }

    @GetMapping("/experience") //GET the experiences of this user
    public ResponseEntity<List<UserExperienceResponse>> getExperience(Principal principal) {
        return ResponseEntity.ok(userProfileService.getExperience(getCurrentUserId(principal))
                .stream()
                .map(userProfileService::toExperienceResponse)
                .toList());
    }

    @DeleteMapping("/experience/{experienceId}") //Delete an experience (experienceId) from this user's list of experience
    public ResponseEntity<Void> deleteExperience(@PathVariable UUID experienceId, Principal principal) {
        userProfileService.deleteExperience(experienceId, getCurrentUserId(principal));
        return ResponseEntity.noContent().build();
    }
}
