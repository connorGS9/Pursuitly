package com.pursuitly.pursuitly.applications;

import com.pursuitly.pursuitly.applications.model.Application;
import com.pursuitly.pursuitly.jobs.model.Job;
import com.pursuitly.pursuitly.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID>, JpaSpecificationExecutor<Application> {
    boolean existsByUserAndJob(User user, Job job);
    List<Application> findByUser(User user);
}
