package com.pursuitly.pursuitly.rateLimiting;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillIntervally(5, Duration.ofDays(1))
                        .build())
                .build();
    }

    public boolean tryConsume(String userId) { //Consumes one token and grants user access to generating a cover letter
        Bucket bucket = buckets.computeIfAbsent(userId, k -> createNewBucket());
        return bucket.tryConsume(1);
    }
}
