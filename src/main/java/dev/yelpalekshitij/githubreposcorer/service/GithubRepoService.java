package dev.yelpalekshitij.githubreposcorer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yelpalekshitij.githubreposcorer.config.AppConfigProperties;
import dev.yelpalekshitij.githubreposcorer.exception.GitHubApiException;
import dev.yelpalekshitij.githubreposcorer.model.GitHubResponse;
import dev.yelpalekshitij.githubreposcorer.model.RepositoryDTO;
import dev.yelpalekshitij.githubreposcorer.model.RepositoryInfo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GithubRepoService implements IGithubRepoService {

    private static final Logger logger = LoggerFactory.getLogger(GithubRepoService.class);

    private final AppConfigProperties configProperties;
    private final IHttpCallerService httpCallerService;
    private final IScoreService scoreService;
    private final ObjectMapper objectMapper;

    private ExecutorService executorService;

    @PostConstruct
    void setup() {
        this.executorService = Executors.newFixedThreadPool(configProperties.getGithub().getParallelism());
    }

    @PreDestroy
    void cleanUp() {
        executorService.shutdown();
    }

    @Override
    @Cacheable(cacheNames = "repoScores", key = "#language + '_' + #date.toString()", sync = true)
    public List<RepositoryDTO> getPopularGithubRepos(String language, LocalDate date) {

        logger.info("Fetching GitHub repositories for language {} and date {}", language, date);

        var maxPages = configProperties.getGithub().getMaxPages();
        var futures = IntStream.rangeClosed(1, maxPages)
                .mapToObj(page -> getPagedGithubResponses(language, date, page))
                .toList();


        return futures.stream()
                .map(f -> {
                    try {
                        return f.join();
                    } catch (CompletionException e) {
                        logger.error("Failed fetching GitHub page", e.getCause());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap(resp -> resp.items().stream())
                .map(this::withPopularity)
                .sorted(Comparator.comparingDouble(RepositoryDTO::popularityScore).reversed())
                .toList();
    }

    private CompletableFuture<GitHubResponse> getPagedGithubResponses(String language, LocalDate date, int page) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching GitHub repositories for language {}, date {} and page {}", language, date, page);
                HttpResponse<String> response = fetchGithubRepos(language, date, page);

                int statusCode = response.statusCode();
                String body = response.body();
                if (statusCode != 200) {
                    handleError(statusCode, body);
                }
                logger.debug("Successfully fetched GitHub repositories for language {}, date {} and page {}", language, date, page);

                return parseResponse(body);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executorService);
    }

    private GitHubResponse parseResponse(String body) {
        GitHubResponse gitHubResponse;
        try {
            gitHubResponse = objectMapper.readValue(body, GitHubResponse.class);
        } catch (IOException e) {
            throw new GitHubApiException(
                    "Failed to parse GitHub API response",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    body
            );
        }
        return gitHubResponse;
    }

    private HttpResponse<String> fetchGithubRepos(String language, LocalDate date, int pageNumber) {
        try {
            String uri = configProperties.getGithub().getApiUrl() + configProperties.getGithub().getRepositoriesPath();
            Map<String, String> queryParams = Map.of(
                    "q", "language:" + language + " created:>=" + date,
                    "sort", "stars",
                    "order", "desc",
                    "page", String.valueOf(pageNumber),
                    "per_page", String.valueOf(configProperties.getGithub().getPerPage())
            );
            Map<String, String> headers = Map.of(
                    HttpHeaders.ACCEPT, "application/vnd.github+json",
                    "X-GitHub-Api-Version", "2022-11-28"
            );

            logger.trace("GET request to URI {} and queryParams {}", uri, queryParams);
            return httpCallerService.get(uri, headers, queryParams, null);
        } catch (IOException | InterruptedException e) {
            throw new GitHubApiException(
                    "Error occurred while fetching GitHub repositories: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null
            );
        }
    }

    private RepositoryDTO withPopularity(RepositoryInfo repo) {
        return new RepositoryDTO(
                repo.name(),
                repo.language(),
                repo.stargazersCount(),
                repo.forksCount(),
                repo.updatedAt(),
                scoreService.calculatePopularityScore(repo)
        );
    }

    private void handleError(int statusCode, String body) {
        switch (statusCode) {
            case 422 -> throw new GitHubApiException("Unprocessable Entity", statusCode, body);
            case 503 -> throw new GitHubApiException("GitHub Service Unavailable", statusCode, body);
            default -> throw new GitHubApiException("Unexpected GitHub API response", statusCode, body);
        }
    }
}
