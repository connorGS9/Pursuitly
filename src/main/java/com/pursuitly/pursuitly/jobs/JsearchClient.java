package com.pursuitly.pursuitly.jobs;

import com.pursuitly.pursuitly.common.enums.RemotePreference;
import com.pursuitly.pursuitly.jobs.model.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JsearchClient implements JobApiClient{

    @Value("${jsearch.api.key}")
    private String apiKey;

    @Value("${jsearch.api.host}")
    private String apiHost;

    @Value("${jsearch.api.url}")
    private String apiUrl;

    private final WebClient webClient;

    public JsearchClient() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(5 * 1024 * 1024))
                .build();
    }

    @Override
    public List<Job> searchJobs(String query) {
            WebClient.RequestHeadersSpec<?> request = webClient
                    .get()
                    .uri(apiUrl + "?query=" + query + "&num_pages=3") // Hardcode 3 pages
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", apiHost);

            Map<String, Object> response = request
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                return data.stream()
                        .map(this::mapToJob)
                        .filter(job -> job != null)
                        .toList();
            }

        return new ArrayList<>();
    }

    @Override
    public String getSourceName() {
        return "Jsearch";
    }

    private Job mapToJob(Map<String, Object> jobData) {
        try {
            String externalId = (String) jobData.get("job_id");
            if (externalId == null) return null;

            Boolean isRemote = (Boolean) jobData.get("job_is_remote");
            Object salaryMin = jobData.get("job_min_salary");
            Object salaryMax = jobData.get("job_max_salary");
            String salaryRange = (salaryMin != null && salaryMax != null)
                    ? salaryMin + " - " + salaryMax : null;

            return Job.builder()
                    .externalId(externalId)
                    .title((String) jobData.get("job_title"))
                    .company((String) jobData.get("employer_name"))
                    .description((String) jobData.get("job_description"))
                    .location((String) jobData.get("job_city"))
                    .remoteType(isRemote != null && isRemote
                            ? RemotePreference.REMOTE : RemotePreference.ONSITE)
                    .salaryRange(salaryRange)
                    .applyUrl((String) jobData.get("job_apply_link"))
                    .source("JSearch")
                    .postedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            System.out.println("Error mapping job: " + e.getMessage());
            return null;
        }
    }
}
