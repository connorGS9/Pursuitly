package com.pursuitly.pursuitly.applications;

import com.pursuitly.pursuitly.applications.model.Application;
import com.pursuitly.pursuitly.common.enums.ApplicationStatus;
import com.pursuitly.pursuitly.jobs.JobsRepository;
import com.pursuitly.pursuitly.jobs.model.Job;
import com.pursuitly.pursuitly.user.UserRepository;
import com.pursuitly.pursuitly.user.model.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobsRepository jobsRepository;
    // Sets this job as applied to by the user from securitycontext in application repo
    public Application applyToJob(UUID jobId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Job job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));

        if (applicationRepository.existsByUserAndJob(user, job)) {
            throw new RuntimeException("Already applied for this job");
        }

        Application application = Application.builder()
                .user(user)
                .job(job)
                .build();

        return applicationRepository.save(application);
    }

    public List<Application> getUserApplications() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return applicationRepository.findByUser(user);
    }

    public Application updateStatus(UUID applicationId, ApplicationStatus status) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));

        if (!application.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized for this application");
        }

        application.setStatus(status);
        return applicationRepository.save(application);
    }

    public void deleteApplication(UUID applicationId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));

        if (!application.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        applicationRepository.delete(application);
    }

}
