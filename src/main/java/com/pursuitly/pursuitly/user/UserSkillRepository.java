package com.pursuitly.pursuitly.user;

import com.pursuitly.pursuitly.user.model.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {
    List<UserSkill> findAllByUserId(UUID userId);
    void deleteByIdAndUserId(UUID id, UUID userId);

    void deleteAllByUserId(UUID userId);
}
