package com.admin.portal.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Startup migration to automatically correct the database schema if needed.
 * Specifically, ensures that the 'experience' column in the 'job_applications'
 * table is enlarged to VARCHAR(255) (or equivalent) so that longer experience
 * strings like "Fresher / 0-1 Years" do not trigger a data truncation error.
 *
 * This is necessary because Hibernate's ddl-auto=update does not alter existing
 * columns' lengths or types in MySQL if the table was created under an older schema.
 */
@Component
public class DatabaseSchemaMigration implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            System.out.println("[DatabaseSchemaMigration] Verifying and updating experience column length...");
            // Execute ALTER TABLE to ensure experience column length is sufficient
            entityManager.createNativeQuery("ALTER TABLE job_applications MODIFY COLUMN experience VARCHAR(255)").executeUpdate();
            System.out.println("[DatabaseSchemaMigration] Database schema migration successfully completed.");
        } catch (Exception e) {
            // Log warning but don't crash startup if table doesn't exist yet or if there's a permissions issue
            System.err.println("[DatabaseSchemaMigration] Warning: Could not run ALTER TABLE migration: " + e.getMessage());
        }
    }
}
