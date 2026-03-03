package com.pursuitly.pursuitly.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class JobMatchController {
    private final JobMatchService jobMatchService;

    @GetMapping("/jobs")
    public ResponseEntity<List<Map<String, Object>>> getMatchedJobs(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(jobMatchService.getMatchedJobs(limit));
    }
}
