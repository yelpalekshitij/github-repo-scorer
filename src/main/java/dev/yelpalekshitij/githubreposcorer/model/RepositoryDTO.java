package dev.yelpalekshitij.githubreposcorer.model;

import java.time.Instant;

public record RepositoryDTO(
        String name,

        String language,

        long stars,

        long forks,

        Instant lastUpdatedAt,

        double popularityScore
) {
}
