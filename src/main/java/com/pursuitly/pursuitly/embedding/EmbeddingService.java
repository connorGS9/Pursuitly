package com.pursuitly.pursuitly.embedding;

import com.pursuitly.pursuitly.user.model.User;
import com.pursuitly.pursuitly.user.model.UserExperience;
import com.pursuitly.pursuitly.user.model.UserProfile;
import com.pursuitly.pursuitly.user.model.UserSkill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {
    @Value("${openai.api.key}")
    private String openAiKey;

    private final WebClient webClient = WebClient.builder().build();

    public float[] generateEmbedding(String text) {
        Map<String, Object> request = Map.of(
                "model", "text-embedding-3-small",
                "input", text
        );

        Map<String, Object> response = webClient.post()
                .uri("https://api.openai.com/v1/embeddings")
                .header("Authorization", "Bearer " + openAiKey)
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        List<Double> embeddingList = (List<Double>) data.get(0).get("embedding");

        float[] embedding = new float[embeddingList.size()];
        for (int i = 0; i < embeddingList.size(); i++) {
            embedding[i] = embeddingList.get(i).floatValue();
        }
        return embedding;
    }

    public float[] generateUserEmbedding(User user, List<UserSkill> skills,
                                         List<UserExperience> experiences,
                                         UserProfile profile) {
        StringBuilder sb = new StringBuilder();

        if (profile != null) {
            if (profile.getSummary() != null) sb.append(profile.getSummary()).append(" ");
            if (profile.getDesiredTitle() != null) sb.append(profile.getDesiredTitle()).append(" ");
        }

        for (UserSkill skill : skills) {
            sb.append(skill.getSkillName()).append(" ");
            sb.append(skill.getYearsExperience()).append(" years ");
        }

        for (UserExperience exp : experiences) {
            sb.append(exp.getTitle()).append(" at ").append(exp.getCompany()).append(" ");
            for (String bullet : exp.getBullets()) {
                sb.append(bullet).append(" ");
            }
        }
        // Generate the embedding from user experience, skills, desired title, and summary
        return generateEmbedding(sb.toString());
    }
}
