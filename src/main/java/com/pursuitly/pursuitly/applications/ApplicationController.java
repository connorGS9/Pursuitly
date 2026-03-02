package com.pursuitly.pursuitly.applications;

import com.pursuitly.pursuitly.applications.model.Application;
import com.pursuitly.pursuitly.common.enums.ApplicationStatus;
import com.pursuitly.pursuitly.jobs.JobsRepository;
import com.pursuitly.pursuitly.jobs.model.Job;
import com.pursuitly.pursuitly.user.UserRepository;
import com.pursuitly.pursuitly.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping // Record that this user is applying to this job --> service calls repository and app will be saved connected to user
    public ResponseEntity<Application> applyToJob(@RequestParam UUID jobId) {
        return ResponseEntity.ok(applicationService.applyToJob(jobId));
    }

    @GetMapping // Get all applications for a user
    public ResponseEntity<List<Application>> getUserApplications() {
        return ResponseEntity.ok(applicationService.getUserApplications());
    }

    @PatchMapping("/{id}/status") // Update the status of application (id) with status (status)
    public ResponseEntity<Application> updateStatus(@PathVariable UUID id, @RequestParam ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}") // Delete application (id) from user's applications
    public ResponseEntity<Void> deleteApplication(@PathVariable UUID id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}
