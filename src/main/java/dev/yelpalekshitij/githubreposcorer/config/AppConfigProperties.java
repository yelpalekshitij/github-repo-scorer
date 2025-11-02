package dev.yelpalekshitij.githubreposcorer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Root configuration mapping for all application-level properties.
 */
@Configuration
@ConfigurationProperties(prefix = "app-config")
@Getter
@Setter
public class AppConfigProperties {

    private GitHubProperties github = new GitHubProperties();
    private PopularityScoreWeightsProperties popularityScoreWeights = new PopularityScoreWeightsProperties();

    @Getter
    @Setter
    public static class GitHubProperties {
        private String apiUrl = "https://example.com";
        private String repositoriesPath = "/";
        private String token;
        private int perPage = 100;
        private int maxPages = 10;
        private int parallelism = 10;
    }

    @Getter
    @Setter
    public static class PopularityScoreWeightsProperties {
        private double stars = 2;
        private double forks = 1;
    }
}
