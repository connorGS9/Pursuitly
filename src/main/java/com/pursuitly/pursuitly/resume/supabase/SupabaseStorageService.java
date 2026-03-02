package com.pursuitly.pursuitly.resume.supabase;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucket;

    private final WebClient webClient = WebClient.builder().build();

    public String getSignedUrl(String userId) { // Signed url so others cant access another persons resume
        String fileName = userId + "/resume.pdf";

        Map<String, Object> response = WebClient.builder().build()
                .post()
                .uri(supabaseUrl + "/storage/v1/object/sign/" + bucket + "/" + fileName)
                .header("Authorization", "Bearer " + supabaseKey)
                .header("Content-Type", "application/json")
                .bodyValue("{\"expiresIn\": 3600}")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        String signedPath = (String) response.get("signedURL");
        System.out.println("Signed path from Supabase: " + signedPath);
        return supabaseUrl + "/storage/v1" + signedPath;
    }

    public String uploadResume(MultipartFile resume, String userId) throws IOException {
            String fileName = userId + "/resume.pdf";
            byte[] fileBytes = resume.getBytes();
            String response = WebClient.builder().build()
                    .post()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("Content-Type", "application/pdf")
                    .header("x-upsert", "true")
                    .bodyValue(fileBytes)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .doOnNext(body -> System.out.println("Supabase error body: " + body))
                                    .flatMap(body -> Mono.error(new RuntimeException("Supabase 400: " + body)))
                    )
                    .bodyToMono(String.class)
                    .block();

            return getSignedUrl(userId);
    }
}
