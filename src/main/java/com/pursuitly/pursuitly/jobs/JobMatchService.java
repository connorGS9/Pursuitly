package com.pursuitly.pursuitly.jobs;

import com.pursuitly.pursuitly.embedding.EmbeddingService;
import com.pursuitly.pursuitly.user.UserExperienceRepository;
import com.pursuitly.pursuitly.user.UserProfileRepository;
import com.pursuitly.pursuitly.user.UserRepository;
import com.pursuitly.pursuitly.user.UserSkillRepository;
import com.pursuitly.pursuitly.user.model.User;
import com.pursuitly.pursuitly.user.model.UserExperience;
import com.pursuitly.pursuitly.user.model.UserProfile;
import com.pursuitly.pursuitly.user.model.UserSkill;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobMatchService {
    private final EmbeddingService embeddingService;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserExperienceRepository userExperienceRepository;
    private final UserProfileRepository userProfileRepository;
    private final EntityManager entityManager;

    @Transactional
    public void updateUserEmbedding() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserSkill> skills = userSkillRepository.findAllByUserId(user.getId());
        List<UserExperience> experiences = userExperienceRepository.findAllByUserIdOrderByStartDateDesc(user.getId());
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);

        float[] embedding = embeddingService.generateUserEmbedding(user, skills, experiences, profile);
        System.out.println("Generated embedding length: " + (embedding != null ? embedding.length : "NULL"));
        user.setEmbedding(Arrays.toString(embedding));
        userRepository.save(user);
    }

    @Transactional
    public List<Map<String, Object>> getMatchedJobs(int limit) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEmbedding() == null) {
           return List.of();
        }

        String vectorStr = "[" + user.getEmbedding().substring(1, user.getEmbedding().length() - 1) + "]";
        System.out.println("Vector string length: " + vectorStr.length());
        System.out.println("Vector string start: " + vectorStr.substring(0, 50));
        List<Object[]> results = entityManager.createNativeQuery(
                        "SELECT id, title, company, location, salary_range, " +
                                "1 - (embedding <=> CAST(:embedding AS vector)) as match_score " +
                                "FROM jobs " +
                                "WHERE embedding IS NOT NULL " +
                                "ORDER BY match_score DESC " +
                                "LIMIT :limit"
                )
                .setParameter("embedding", vectorStr)
                .setParameter("limit", limit)
                .getResultList();

        return results.stream().map(row -> {
            Map<String, Object> job = new HashMap<>();
            job.put("id", row[0]);
            job.put("title", row[1]);
            job.put("company", row[2]);
            job.put("location", row[3]);
            job.put("salaryRange", row[4]);
            job.put("matchScore", row[5]);
            return job;
        }).toList();
    }
}
