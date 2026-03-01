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
public class UsaJobsClient implements JobApiClient{

    @Value("${usajobs.api.url}")
    private String usaJobsUrl;

    @Value("${usajobs.api.email}")
    private String usaJobsEmail;

    @Value("${usajobs.api.key}")
    private String usaJobsKey;

    private final WebClient webClient;

    public UsaJobsClient() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(5 * 1024 * 1024)) // Add memory to buffer since job descriptions > 256k Bytes default
                .build();
    }

    @Override
    public List<Job> searchJobs(String query) {
        try {
            System.out.println("Calling UsaJobs for: " + query);

            Map<String, Object> response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("data.usajobs.gov")
                            .path("/api/search")
                            .queryParam("Keyword", query)
                            .queryParam("ResultsPerPage", 50)
                            .build())
                    .header("Host", "data.usajobs.gov")
                    .header("User-Agent", usaJobsEmail)
                    .header("Authorization-Key", usaJobsKey)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            System.out.println("USAJobs raw response: " + response);

            if (response != null && response.containsKey("SearchResult")) {
                System.out.println("Top level keys: " + response.keySet());
                Map<String, Object> searchResult = (Map<String, Object>) response.get("SearchResult");
                System.out.println("SearchResult keys: " + searchResult.keySet());
                List<Map<String, Object>> results = (List<Map<String, Object>>) searchResult.get("SearchResultItems");
                System.out.println("UsaJobs results count: " + results.size());
                return results.stream()
                        .map(this::mapToJob)
                        .filter(job -> job != null)
                        .toList();
            }
        } catch (Exception e) {
            System.out.println("USAJobs error: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private Job mapToJob(Map<String, Object> item) {
        try {
            System.out.println("Item keys: " + item.keySet());

            String externalId = String.valueOf(item.get("MatchedObjectId"));
            System.out.println("ExternalId: " + externalId);

            Map<String, Object> descriptor = (Map<String, Object>) item.get("MatchedObjectDescriptor");
            System.out.println("Descriptor null? " + (descriptor == null));

            if (descriptor == null) return null;
            System.out.println("Descriptor keys: " + descriptor.keySet());

            String title = (String) descriptor.get("PositionTitle");
            System.out.println("Title: " + title);
            String company = (String) descriptor.get("OrganizationName");
            String location = (String) descriptor.get("PositionLocationDisplay");

            // Salary is in a list
            List<Map<String, Object>> pay = (List<Map<String, Object>>) descriptor.get("PositionRemuneration");
            String salaryRange = null;
            if (pay != null && !pay.isEmpty()) {
                salaryRange = pay.get(0).get("MinimumRange") + " - " + pay.get(0).get("MaximumRange");
            }

            // Description is nested
            Map<String, Object> userArea = (Map<String, Object>) descriptor.get("UserArea");
            Map<String, Object> details = (Map<String, Object>) userArea.get("Details");
            String description = (String) details.get("JobSummary");

            // Apply URL is a list
            List<String> applyUris = (List<String>) descriptor.get("ApplyURI");
            String applyUrl = (applyUris != null && !applyUris.isEmpty()) ? applyUris.get(0) : null;

            return Job.builder()
                    .externalId("usajobs_" + externalId)
                    .title(title)
                    .company(company)
                    .description(description)
                    .location(location)
                    .remoteType(RemotePreference.ANY)
                    .salaryRange(salaryRange)
                    .applyUrl(applyUrl)
                    .source("USAJobs")
                    .postedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            System.out.println("Error mapping USAJobs job: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getSourceName() {
        return "USAJobs";
    }
}
