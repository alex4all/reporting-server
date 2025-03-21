package org.reporting.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.reporting.repository.PersonRepository;
import org.reporting.repository.ReportsRepository;

@Configuration
public class DatabaseConfig {
    
    @Value("${spring.datasource.url}")
    private String dbUrl;
    
    @Value("${spring.datasource.username}")
    private String dbUser;
    
    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Bean
    public PersonRepository personRepository() {
        return new PersonRepository(dbUrl, dbUser, dbPassword);
    }

    @Bean
    public ReportsRepository reportsRepository() {
        return new ReportsRepository();
    }
} 