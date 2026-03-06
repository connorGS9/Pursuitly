package com.pursuitly.pursuitly.coverLetter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cover-letter")
@RequiredArgsConstructor
public class CoverLetterController {
    private final CoverLetterService coverLetterService;

    @PostMapping("/generate")
    public ResponseEntity<String> generate(@RequestParam UUID jobId) {
            return ResponseEntity.ok(coverLetterService.generateCoverLetter(jobId));
        }

    @PostMapping("/generate-manual")
    public ResponseEntity<String> generateManual(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(coverLetterService.generateCoverLetterFromDescription(body.get("jobDescription")));
    }
}
