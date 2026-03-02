package com.pursuitly.pursuitly.jobs.model;

import com.pursuitly.pursuitly.common.enums.RemotePreference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String externalId;

    private String title;
    private String company;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    @Enumerated(EnumType.STRING)
    private RemotePreference remoteType;

    private String salaryRange;
    private String source;

    @Column(columnDefinition = "TEXT")
    private String applyUrl;

    private LocalDateTime postedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "vector(1536)") // PGvector embedding
    private float[] embedding;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}