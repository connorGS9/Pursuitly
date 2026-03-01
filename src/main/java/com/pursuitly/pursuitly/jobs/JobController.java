package com.pursuitly.pursuitly.jobs;

import com.pursuitly.pursuitly.jobs.model.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JsearchClient jsearchClient;
    private final JobAggregatorService jobAggregatorService;
    private final AdzunaClient adzunaClient;
    private final UsaJobsClient usaJobsClient;

    @PostMapping("/aggregate")
    public ResponseEntity<String> triggerAggregation() {
        jobAggregatorService.aggregateJobs();
        return ResponseEntity.ok("Aggregation complete");
    }

    @PostMapping("/aggregate/adzuna")
    public ResponseEntity<String> testAdzuna() {
        List<Job> jobs = adzunaClient.searchJobs("software engineer");
        System.out.println("Adzuna returned: " + jobs.size() + " jobs");
        return ResponseEntity.ok("Adzuna returned: " + jobs.size() + " jobs");
    }

    @PostMapping("/aggregate/usajobs")
    public ResponseEntity<String> testUsajobs() {
        List<Job> jobs = usaJobsClient.searchJobs("software engineer");
        System.out.println("USAJobs returned: " + jobs.size() + " jobs");
        return ResponseEntity.ok("USAJobs returned: " + jobs.size() + " jobs");
    }
}
