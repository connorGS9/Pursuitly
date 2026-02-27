package com.pursuitly.pursuitly.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JsearchClient {

    @Value("${jsearch.api.key}")
    private String apiKey;

    @Value("${jsearch.api.host}")
    private String apiHost;

    @Value("${jsearch.api.url}")
    private String apiUrl;

    private final WebClient webClient;

    public JsearchClient() {
        this.webClient = WebClient.builder().build();
    }

    public List<Map<String, Object>> searchJobs(String query, int numPages) {
        try {
            WebClient.RequestHeadersSpec<?> request = webClient
                    .get()
                    .uri(apiUrl + "?query=" + query + "&num_pages=" + numPages)
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", apiHost);

            Map<String, Object> response = request
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null && response.containsKey("data")) {
                return (List<Map<String, Object>>) response.get("data");
            }
        } catch (Exception e) {
            System.out.println("JSearch API error: " + e.getMessage());
        }
        return new ArrayList<>();
    }
}
