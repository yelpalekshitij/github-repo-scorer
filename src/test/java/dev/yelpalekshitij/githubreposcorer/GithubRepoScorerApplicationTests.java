package dev.yelpalekshitij.githubreposcorer;

import dev.yelpalekshitij.githubreposcorer.model.GitHubResponse;
import dev.yelpalekshitij.githubreposcorer.model.RepositoryInfo;
import dev.yelpalekshitij.githubreposcorer.service.IGithubRepoService;
import dev.yelpalekshitij.githubreposcorer.service.IHttpCallerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class GithubRepoScorerApplicationTests {

    @Autowired
    private IGithubRepoService githubRepoService;

    @MockitoBean
    private IHttpCallerService httpCallerService;

    private final GitHubResponse mockResponse = mock(GitHubResponse.class);

    private final HttpResponse<String> httpResponse = mock(HttpResponse.class);


    @Test
    void shouldReturnCachedDataFromCacheManagerInConcurrentContext() throws InterruptedException, IOException {

        String jsonResponse = """
            {"items":[{"id": 1087543398,"name":"repo1","full_name":"fullName/repo1","private":"false","html_url":"https://example.com","language":"Java","stargazersCount":10,"forksCount":5,"updated_at":"2025-10-31T10:00:00Z"}]}
        """;

        when(mockResponse.items()).thenReturn(List.of(
                new RepositoryInfo(1087543398, "repo1","Java","fullName/repo1", "https://example.com", 10,5, Instant.parse("2025-10-31T10:00:00Z"))
        ));
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(jsonResponse);
        when(httpCallerService.get(any(), any(), any(), any())).thenReturn(httpResponse);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var latch = new CountDownLatch(2);

            for (int i = 0; i < 2; i++) {
                executor.submit(() -> {
                    try {
                        githubRepoService.getPopularGithubRepos("java", LocalDate.of(2025, 10, 31));
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(); // wait for both tasks to finish
        }

        verify(httpCallerService, times(1)).get(any(), any(), any(), any());
    }
}
