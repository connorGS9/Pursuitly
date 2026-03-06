package com.pursuitly.pursuitly.jobs;

import com.pursuitly.pursuitly.jobs.model.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobAggregatorService jobAggregatorService;
    private final JobsRepository jobsRepository;

    @PostMapping("/aggregate")
    public ResponseEntity<String> triggerAggregation() {
        jobAggregatorService.aggregateJobs();
        return ResponseEntity.ok("Aggregation complete");
    }

    @PostMapping("/aggregateJsearch")
    public ResponseEntity<String> triggerJsearch() {
        jobAggregatorService.aggregateJsearchJobs();
        return ResponseEntity.ok("Jsearch Aggregation complete");
    }

    // When trying to get jobs, you can search by three non-required parameters: title, location, salary to narrow search down
    @GetMapping
    public ResponseEntity<List<Job>> getJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String salary){
        Specification<Job> spec = (root, query, cb) -> cb.conjunction();

        if (title != null && !title.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        }
        if (location != null && !location.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
        }
        if (salary != null && !salary.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("salaryRange")), "%" + salary.toLowerCase() + "%"));
        }
        return ResponseEntity.ok(jobsRepository.findAll(spec));
    }
}
