package com.pursuitly.pursuitly.coverLetter;

import com.pursuitly.pursuitly.jobs.JobsRepository;
import com.pursuitly.pursuitly.jobs.model.Job;
import com.pursuitly.pursuitly.rateLimiting.RateLimitService;
import com.pursuitly.pursuitly.user.UserExperienceRepository;
import com.pursuitly.pursuitly.user.UserProfileRepository;
import com.pursuitly.pursuitly.user.UserRepository;
import com.pursuitly.pursuitly.user.UserSkillRepository;
import com.pursuitly.pursuitly.user.model.User;
import com.pursuitly.pursuitly.user.model.UserExperience;
import com.pursuitly.pursuitly.user.model.UserSkill;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoverLetterService {
    @Value("${openai.api.key}")
    private String openAiKey;

    private final JobsRepository jobsRepository;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserExperienceRepository userExperienceRepository;
    private final UserProfileRepository userProfileRepository;
    private final RateLimitService rateLimitService;

    private final WebClient webClient = WebClient.builder().build();

    // Constructs as cover letter based on user experience, skills, and profile compared to job description using GPT 4o mini
    @Transactional
    public String generateCoverLetter(UUID jobId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!rateLimitService.tryConsume(user.getId().toString())) { //Rate limiter 5 requests per user per day
            throw new RuntimeException("Daily cover letter limit reached. Try again tomorrow.");
        }

        Job job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job not found"));

        List<UserSkill> skills = userSkillRepository.findAllByUserId(user.getId());
        List<UserExperience> experiences = userExperienceRepository.findAllByUserIdOrderByStartDateDesc(user.getId());

        String prompt = buildPrompt(user, job, skills, experiences);

        Map<String, Object> request = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a professional cover letter writer. Write concise, tailored cover letters. The cover letter must fit on a single page — aim for 3-4 short paragraphs, no more than 400 words total. Format the letter properly with: a date, recipient greeting (Dear Hiring Manager,), the body paragraphs, a closing (Sincerely,), and a signature line with the candidate's name."),
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 600
        );

        Map<String, Object> response = webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + openAiKey)
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    @Transactional
    public String generateCoverLetterFromDescription(String jobDescription) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!rateLimitService.tryConsume(user.getId().toString())) {
            throw new RuntimeException("Daily cover letter limit reached. Try again tomorrow.");
        }

        List<UserSkill> skills = userSkillRepository.findAllByUserId(user.getId());
        List<UserExperience> experiences = userExperienceRepository.findAllByUserIdOrderByStartDateDesc(user.getId());

        String prompt = buildPromptFromDescription(user, jobDescription, skills, experiences);

        Map<String, Object> request = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a professional cover letter writer. Write concise, tailored cover letters. The cover letter must fit on a single page — aim for 3-4 short paragraphs, no more than 400 words total. Format the letter properly with: a date, recipient greeting (Dear Hiring Manager,), the body paragraphs, a closing (Sincerely,), and a signature line with the candidate's name."),
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 600
        );

        Map<String, Object> response = webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + openAiKey)
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private String buildPromptFromDescription(User user, String jobDescription, List<UserSkill> skills, List<UserExperience> experiences) {
        StringBuilder sb = new StringBuilder();
        sb.append("Write a professional cover letter for ").append(user.getFullName()).append(".\n\n");
        sb.append("Job Description:\n").append(jobDescription).append("\n\n");

        userProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            sb.append("Candidate Summary: ").append(profile.getSummary()).append("\n");
            sb.append("Desired Title: ").append(profile.getDesiredTitle()).append("\n\n");
        });

        sb.append("Candidate Skills:\n");
        for (UserSkill skill : skills) {
            sb.append("- ").append(skill.getSkillName())
                    .append(" (").append(skill.getProficiency()).append(", ")
                    .append(skill.getYearsExperience()).append(" years)\n");
        }

        sb.append("\nWork Experience:\n");
        for (UserExperience exp : experiences) {
            sb.append("- ").append(exp.getTitle()).append(" at ").append(exp.getCompany()).append("\n");
            for (String bullet : exp.getBullets()) {
                sb.append("  • ").append(bullet).append("\n");
            }
        }

        return sb.toString();
    }

    private String buildPrompt(User user, Job job, List<UserSkill> skills, List<UserExperience> experiences) {
        StringBuilder sb = new StringBuilder();
        sb.append("Write a professional cover letter for ").append(user.getFullName()).append(".\n\n");
        sb.append("Job Title: ").append(job.getTitle()).append("\n");
        sb.append("Company: ").append(job.getCompany()).append("\n");
        sb.append("Job Description: ").append(job.getDescription(), 0, Math.min(job.getDescription().length(), 1000)).append("\n\n");

        userProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            sb.append("Candidate Summary: ").append(profile.getSummary()).append("\n");
            sb.append("Desired Title: ").append(profile.getDesiredTitle()).append("\n\n");
        });

        sb.append("Candidate Skills:\n");
        for (UserSkill skill : skills) {
            sb.append("- ").append(skill.getSkillName())
                    .append(" (").append(skill.getProficiency()).append(", ")
                    .append(skill.getYearsExperience()).append(" years)\n");
        }

        sb.append("\nWork Experience:\n");
        for (UserExperience exp : experiences) {
            sb.append("- ").append(exp.getTitle()).append(" at ").append(exp.getCompany()).append("\n");
            for (String bullet : exp.getBullets()) {
                sb.append("  • ").append(bullet).append("\n");
            }
        }

        return sb.toString();
    }
}
