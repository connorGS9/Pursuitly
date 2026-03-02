package com.pursuitly.pursuitly.resume;

import com.pursuitly.pursuitly.resume.supabase.SupabaseStorageService;
import com.pursuitly.pursuitly.user.UserRepository;
import com.pursuitly.pursuitly.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.IOException;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final SupabaseStorageService supabaseStorageService;
    private final UserRepository userRepository;

    @GetMapping("/url")
    public ResponseEntity<String> getResumeUrl() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(supabaseStorageService.getSignedUrl(user.getId().toString()));
    }
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadResume(@RequestParam("file")MultipartFile file) throws IOException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String url = supabaseStorageService.uploadResume(file, user.getId().toString());
        user.setResumeUrl(url);
        userRepository.save(user);

        return ResponseEntity.ok(url);
    }

}
