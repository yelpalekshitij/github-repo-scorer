package dev.yelpalekshitij.githubreposcorer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@EnableConfigurationProperties
@SpringBootApplication
@EnableCaching
public class GithubRepoScorerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GithubRepoScorerApplication.class, args);
    }
}
