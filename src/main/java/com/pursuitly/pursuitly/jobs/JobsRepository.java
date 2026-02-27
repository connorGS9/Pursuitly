package com.pursuitly.pursuitly.jobs;

import com.pursuitly.pursuitly.common.enums.RemotePreference;
import com.pursuitly.pursuitly.jobs.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobsRepository extends JpaRepository<Job, UUID> {
    boolean existsByExternalId(String externalId);
    List<Job> findByTitleContainingIgnoreCase(String title);
    List<Job> findByRemoteType(RemotePreference remoteType);
    List<Job> findByTitleContainingIgnoreCaseAndRemoteType(String title, RemotePreference remoteType);
}
