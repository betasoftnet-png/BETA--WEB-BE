package com.admin.portal.config;

import com.admin.portal.repository.AdminJobApplicationRepository;
import com.admin.portal.repository.JobRepository;
import com.admin.portal.entity.JobApplication;
import com.admin.portal.entity.Job;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * One-time idempotent startup migration.
 *
 * Backfills job_title / job_department / job_location into every
 * job_applications row that currently has those columns blank/null.
 *
 * Two scenarios handled:
 *  1. Job still exists in DB (active OR soft-deleted):
 *     reads title/dept/location from the jobs table and saves.
 *
 *  2. Job was hard-deleted from DB (IDs 2, 3, 6, 8, 9, 10):
 *     applies known legacy titles inferred from application data.
 *
 * The update is guarded by checking jobTitle IS NULL OR blank,
 * so it is completely safe to run on every startup.
 * Once a row is filled it will never be touched again.
 */
@Component
public class JobTitleMigration {

    @Autowired
    private AdminJobApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    /**
     * Titles for jobs that were permanently (hard) deleted from the DB
     * before the job_title column was introduced in JobApplication.
     * Keys are job IDs; values are [title, department, location].
     */
    private static final Map<Long, String[]> LEGACY_JOB_TITLES = Map.of(
        2L,  new String[]{"Backend Developer",   "Engineering", "Chennai"},
        3L,  new String[]{"UI/UX Designer",       "Design",      "Chennai"},
        6L,  new String[]{"System Engineer",      "Engineering", "Chennai"},
        8L,  new String[]{"Cloud Engineer",       "Engineering", "Remote"},
        9L,  new String[]{"Senior Developer",     "Engineering", "Chennai"},
        10L, new String[]{"DevOps Engineer",      "Engineering", "Remote"}
    );

    @PostConstruct
    @Transactional
    public void backfillJobTitles() {
        List<JobApplication> apps = applicationRepository.findAll().stream()
                .filter(a -> a.getJobId() != null
                          && (a.getJobTitle() == null || a.getJobTitle().isBlank()))
                .collect(java.util.stream.Collectors.toList());

        if (apps.isEmpty()) {
            return;
        }

        int filled = 0;
        for (JobApplication app : apps) {
            Long jobId = app.getJobId();

            // Try the DB first (covers active + soft-deleted jobs)
            Optional<Job> jobOpt = jobRepository.findById(jobId);
            if (jobOpt.isPresent()) {
                Job job = jobOpt.get();
                if (app.getJobTitle() == null || app.getJobTitle().isBlank())
                    app.setJobTitle(job.getTitle());
                if (app.getJobDepartment() == null || app.getJobDepartment().isBlank())
                    app.setJobDepartment(job.getDepartment());
                if (app.getJobLocation() == null || app.getJobLocation().isBlank())
                    app.setJobLocation(job.getLocation());
                filled++;
            } else if (LEGACY_JOB_TITLES.containsKey(jobId)) {
                // Job was hard-deleted; use known legacy mapping
                String[] meta = LEGACY_JOB_TITLES.get(jobId);
                if (app.getJobTitle()      == null || app.getJobTitle().isBlank())      app.setJobTitle(meta[0]);
                if (app.getJobDepartment() == null || app.getJobDepartment().isBlank()) app.setJobDepartment(meta[1]);
                if (app.getJobLocation()   == null || app.getJobLocation().isBlank())   app.setJobLocation(meta[2]);
                filled++;
            }
        }

        if (filled > 0) {
            applicationRepository.saveAll(apps);
            System.out.println("[JobTitleMigration] Backfilled job titles for " + filled + " application(s).");
        }
    }
}
