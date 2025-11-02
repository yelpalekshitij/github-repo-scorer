package dev.yelpalekshitij.githubreposcorer.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RepositoryInfo(
        long id,

        String name,

        String language,

        @JsonProperty("full_name")
        String fullName,

        @JsonProperty("html_url")
        String htmlUrl,

        @JsonAlias("stargazers_count")
        long stargazersCount,

        @JsonAlias("forks_count")
        long forksCount,

        @JsonProperty("updated_at")
        Instant updatedAt
) {}
