package com.pursuitly.pursuitly.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JsearchClient jsearchClient;

    @GetMapping("/test")
    public ResponseEntity<List<Map<String, Object>>> testSearch() {
        List<Map<String, Object>> results = jsearchClient.searchJobs("java developer", 1);
        return ResponseEntity.ok(results);
    }
}
