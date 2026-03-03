package com.pursuitly.pursuitly.user;

import com.pursuitly.pursuitly.user.model.UserExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserExperienceRepository extends JpaRepository<UserExperience, UUID> {
    List<UserExperience> findAllByUserIdOrderByStartDateDesc(UUID userId);
    void deleteByIdAndUserId(UUID id, UUID userId);

    void deleteAllByUserId(UUID userId);
}
