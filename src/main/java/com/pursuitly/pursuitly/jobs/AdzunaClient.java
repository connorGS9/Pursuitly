package com.pursuitly.pursuitly.jobs;

import com.pursuitly.pursuitly.common.enums.RemotePreference;
import com.pursuitly.pursuitly.jobs.model.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AdzunaClient implements JobApiClient{
    @Value("${adzuna.api.id}")
    private String appId;

    @Value("${adzuna.api.key}")
    private String appKey;

    @Value("${adzuna.api.url}")
    private String apiUrl;

    private final WebClient webClient;

    public AdzunaClient() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(5 * 1024 * 1024)) // Add memory to buffer since job descriptions > 256k Bytes default
                .build();
    }


    @Override
    public List<Job> searchJobs(String query) {
        try {
            System.out.println("Calling Adzuna for: " + query);

            Map<String, Object> response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.adzuna.com")
                            .path("/v1/api/jobs/us/search/1")
                            .queryParam("app_id", appId)
                            .queryParam("app_key", appKey)
                            .queryParam("what", query)
                            .queryParam("results_per_page", 50)
                            .queryParam("content-type", "application/json")
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            System.out.println("Adzuna response keys: " +
                    (response != null ? response.keySet() : "null"));
            if (response != null && response.containsKey("results")) {
                List<Map<String, Object>> results =
                        (List<Map<String, Object>>) response.get("results");
                System.out.println("Adzuna results count: " + results.size());
                return results.stream()
                        .map(this::mapToJob)
                        .filter(job -> job != null)
                        .toList();
            }
        } catch (Exception e) {
            System.out.println("Adzuna error: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private Job mapToJob(Map<String, Object> jobData) {
        try {
            String externalId = (String) jobData.get("id");
            if (externalId == null) return null;

            Object salaryMin = jobData.get("salary_min");
            Object salaryMax = jobData.get("salary_max");
            String salaryRange = (salaryMin != null && salaryMax != null)
                    ? salaryMin + " - " + salaryMax : null;

            Map<String, Object> locationData = (Map<String, Object>) jobData.get("location");
            String location = locationData != null
                    ? (String) locationData.get("display_name") : null;

            Map<String, Object> company = (Map<String, Object>) jobData.get("company");
            String companyName = company != null ? (String) company.get("display_name") : null;

            return Job.builder()
                    .externalId("adzuna_" + externalId)
                    .title((String) jobData.get("title"))
                    .company(companyName)
                    .description((String) jobData.get("description"))
                    .location(location)
                    .remoteType(RemotePreference.ANY)
                    .salaryRange(salaryRange)
                    .applyUrl((String) jobData.get("redirect_url"))
                    .source("Adzuna")
                    .postedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            System.out.println("Error mapping Adzuna job: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getSourceName() {
        return "Adzuna";
    }
}
