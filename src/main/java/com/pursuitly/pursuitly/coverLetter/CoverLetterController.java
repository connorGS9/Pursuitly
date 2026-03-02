package com.pursuitly.pursuitly.coverLetter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    }
