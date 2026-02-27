package com.pursuitly.pursuitly.user;

import com.pursuitly.pursuitly.user.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository  extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}
