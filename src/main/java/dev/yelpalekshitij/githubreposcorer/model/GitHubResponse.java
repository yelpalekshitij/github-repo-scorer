package dev.yelpalekshitij.githubreposcorer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubResponse(
        @JsonProperty("total_count")
        int totalCount,

        @JsonProperty("incomplete_results")
        boolean incompleteResults,

        List<RepositoryInfo> items
) {}
