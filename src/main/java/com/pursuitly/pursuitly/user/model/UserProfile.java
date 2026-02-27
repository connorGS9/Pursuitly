package com.pursuitly.pursuitly.user.model;

import com.pursuitly.pursuitly.user.model.enums.ExperienceLevel;
import com.pursuitly.pursuitly.user.model.enums.RemotePreference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String desiredTitle;

    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;

    private String location;

    @Enumerated(EnumType.STRING)
    private RemotePreference remotePreference;

    private Integer salaryMin;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}