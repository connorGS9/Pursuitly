package com.pursuitly.pursuitly.jobs;

import com.pursuitly.pursuitly.jobs.model.Job;

import java.util.List;
import java.util.Map;

public interface JobApiClient {
    // Method to send the query from the application to the API
    /*
        Some APIs use different pagination: Jsearch query = ("Job title", numPages) where each page holds 10 jobs, max = 3
        So each API will handle its own pagination when creating the query in its service
     */
    List<Job> searchJobs(String query);

    String getSourceName();
}
