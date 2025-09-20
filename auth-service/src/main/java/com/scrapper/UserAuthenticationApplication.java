package com.scrapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.function.context.config.ContextFunctionCatalogAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = ContextFunctionCatalogAutoConfiguration.class)
@EnableJpaAuditing
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = "com.scrapper.authservice.repository")
@EntityScan(basePackages      = "com.scrapper.authservice.entity")
@EnableCaching
public class UserAuthenticationApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserAuthenticationApplication.class, args);
    }

}
