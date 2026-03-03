package com.pursuitly.pursuitly.jobs;

import com.pursuitly.pursuitly.embedding.EmbeddingService;
import com.pursuitly.pursuitly.jobs.model.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobAggregatorService {

    private final List<JobApiClient> jobApiClients;
    private final JobsRepository jobRepository;
    private final EmbeddingService embeddingService;
    private final JsearchClient jsearchClient;
    // At 8am and 8 pm every day run this scheduled service
    @Scheduled(cron= "0 0 8,20 * * *") // cron "second, minute, hour (8am, 8pm), day-of-month, month, day-of-week"
    public void aggregateJobs() {
        System.out.println("Running DAILY job aggregation...");
        // Create list of job titles to query for
        List<String> queries = List.of(
                "software engineer",
                "software developer",
                "data engineer",
                "IT support",
                "devops engineer"
        );
        // Create a search for each job title through the 2 daily APIs Adzuna and USAjobs
        List<JobApiClient> dailyClients = jobApiClients.stream()
                .filter(client -> !(client instanceof JsearchClient))
                .toList();

        for (JobApiClient client : jobApiClients) {
            for (String query : queries) {
                List<Job> jobs = client.searchJobs(query);
                for (Job j :jobs) {
                    saveJobIfNotExists(j); // Call save to DB if job is new
                }
            }
        }
        System.out.println("DAILY Job aggregation complete.");
    }

    @Scheduled(cron = "0 0 20 * * *") //Once daily at 8pm run Jsearch API call with 5 title queries
    public void aggregateJsearchJobs() {
        System.out.println("Running JSearch aggregation...");
        List<String> queries = List.of(
                "junior software engineer",
                "junior developer",
                "entry level software engineer",
                "data analyst",
                "cloud engineer"
        );

        for (String query : queries) {
            List<Job> jobs = jsearchClient.searchJobs(query);
            for (Job job : jobs) {
                saveJobIfNotExists(job);
            }
        }
        System.out.println("JSearch aggregation complete.");
    }

    // Creates job object and saves it to DB
    private void saveJobIfNotExists(Job job) {
        if (job.getExternalId() == null
                || jobRepository.existsByExternalId(job.getExternalId())) {
            return;
        }

        if (job.getDescription() != null && !job.getDescription().isBlank()) { // Generate embedding for job description
            float[] embedding = embeddingService.generateEmbedding(job.getDescription());
            job.setEmbedding(embedding);
        }
        jobRepository.save(job);
    }
}
