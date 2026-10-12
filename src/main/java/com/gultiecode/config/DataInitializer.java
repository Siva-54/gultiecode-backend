package com.gultiecode.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DataInitializer has been removed for production readiness.
 * Database initial setup (Admin account, default roles, default domains) 
 * should be executed manually via SQL scripts in scripts/ directory.
 */
@Component
public class DataInitializer implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        // Production mode: No hardcoded automatic data seeding.
    }
}
