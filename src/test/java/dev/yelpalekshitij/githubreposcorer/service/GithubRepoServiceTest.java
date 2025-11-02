package dev.yelpalekshitij.githubreposcorer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.yelpalekshitij.githubreposcorer.config.AppConfigProperties;
import dev.yelpalekshitij.githubreposcorer.model.GitHubResponse;
import dev.yelpalekshitij.githubreposcorer.model.RepositoryInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GithubRepoServiceTest {

    @Mock
    private AppConfigProperties configProperties;

    @Mock
    private IHttpCallerService httpCallerService;

    @Mock
    private IScoreService scoreService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GithubRepoService sut;

    private final GitHubResponse mockResponse = mock(GitHubResponse.class);

    private final HttpResponse<String> httpResponse = mock(HttpResponse.class);

    @BeforeEach
    void setup() {
        when(configProperties.getGithub()).thenReturn(new AppConfigProperties.GitHubProperties() {{
            setPerPage(2);
            setMaxPages(1);
            setParallelism(2);
        }});
        sut.setup();
    }

    @AfterEach
    void cleanup() {
        sut.cleanUp();
    }

    @Test
    void testGetPopularGithubRepos() throws Exception {
        // given
        String jsonResponse = """
            {"items":[{"id": 1087543398,"name":"repo1","full_name":"fullName/repo1","private":"false","html_url":"https://example.com","language":"Java","stargazersCount":10,"forksCount":5,"updatedAt":"2025-10-31T10:00:00Z"}]}
        """;

        when(mockResponse.items()).thenReturn(List.of(
                new RepositoryInfo(1087543398, "repo1","Java","fullName/repo1", "https://example.com", 10,5, Instant.parse("2025-10-31T10:00:00Z"))
        ));
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(jsonResponse);
        when(httpCallerService.get(any(), any(), any(), any())).thenReturn(httpResponse);
        when(objectMapper.readValue(jsonResponse, GitHubResponse.class)).thenReturn(mockResponse);
        when(scoreService.calculatePopularityScore(any())).thenReturn(100.0);

        // when
        var result = sut.getPopularGithubRepos("Java", LocalDate.of(2025,10,31));

        // then
        assertEquals(1, result.size());
        assertEquals("repo1", result.getFirst().name());
        assertEquals(100.0, result.getFirst().popularityScore());
        verify(httpCallerService, times(1)).get(any(), any(), any(), any());
    }

    @Test
    void shouldCombineAllPageResults() throws Exception {
        // given
        var pages = 3;
        when(configProperties.getGithub()).thenReturn(new AppConfigProperties.GitHubProperties() {{
            setPerPage(2);
            setMaxPages(pages);
            setParallelism(2);
        }});
        String jsonResponse = """
            {"items":[{"id": 1087543398,"name":"repo1","full_name":"fullName/repo1","private":"false","html_url":"https://example.com","language":"Java","stargazersCount":10,"forksCount":5,"updatedAt":"2025-10-31T10:00:00Z"}]}
        """;

        when(mockResponse.items()).thenReturn(List.of(
                new RepositoryInfo(1087543398, "repo1","Java","fullName/repo1", "https://example.com", 10,5, Instant.parse("2025-10-31T10:00:00Z"))
        ));
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(jsonResponse);
        when(httpCallerService.get(any(), any(), any(), any())).thenReturn(httpResponse);
        when(objectMapper.readValue(jsonResponse, GitHubResponse.class)).thenReturn(mockResponse);
        when(scoreService.calculatePopularityScore(any())).thenReturn(100.0);

        // when
        var result = sut.getPopularGithubRepos("Java", LocalDate.of(2025,10,31));

        // then
        assertEquals(pages, result.size());
        assertEquals("repo1", result.getFirst().name());
        assertEquals(100.0, result.getFirst().popularityScore());
        verify(httpCallerService, times(pages)).get(any(), any(), any(), any());
    }

    @Test
    void shouldReturnEmptyListIfNoResultFound() throws IOException, InterruptedException {
        // given
        String jsonResponse = """
            {"items":[]}
        """;

        when(mockResponse.items()).thenReturn(List.of());
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(jsonResponse);
        when(httpCallerService.get(any(), any(), any(), any())).thenReturn(httpResponse);
        when(objectMapper.readValue(jsonResponse, GitHubResponse.class)).thenReturn(mockResponse);

        // when
        var result = sut.getPopularGithubRepos("Java", LocalDate.of(2025,10,31));

        // then
        assertEquals(0, result.size());
        verify(scoreService, never()).calculatePopularityScore(any());
        verify(httpCallerService, times(1)).get(any(), any(), any(), any());
    }

    @Test
    void shouldThrowGitHubApiExceptionOnHttpIOException() throws IOException, InterruptedException {
        // given
        when(httpCallerService.get(any(), any(), any(), any()))
                .thenThrow(new IOException("Connection failed"));

        // when
        var result = sut.getPopularGithubRepos("Java", LocalDate.of(2025, 10, 31));

        // then
        assertTrue(result.isEmpty());
        verify(httpResponse, never()).statusCode();
        verify(httpResponse, never()).body();
        verify(httpCallerService, times(1)).get(any(), any(), any(), any());
    }

    @ParameterizedTest
    @CsvSource({
            "422, {\"message\":\"error\"}",
            "503, {\"message\":\"Service Unavailable\"}",
            "599, {\"message\":\"Some unknown\"}"
    })
    void shouldHandleGitHubApiErrorResponses(int statusCode, String responseBody) throws IOException, InterruptedException {
        // given
        when(httpResponse.statusCode()).thenReturn(statusCode);
        when(httpResponse.body()).thenReturn(responseBody);
        when(httpCallerService.get(any(), any(), any(), any())).thenReturn(httpResponse);

        // when
        var result = sut.getPopularGithubRepos("Java", LocalDate.of(2025, 10, 31));

        // then
        assertTrue(result.isEmpty());
        verify(objectMapper, never()).readValue(anyString(), eq(GitHubResponse.class));
        verify(httpCallerService, times(1)).get(any(), any(), any(), any());
    }

    @Test
    void shouldThrowGitHubApiExceptionOnParsingError() throws IOException, InterruptedException {
        // given
        String invalidJson = "invalid json";
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(invalidJson);
        when(httpCallerService.get(any(), any(), any(), any())).thenReturn(httpResponse);
        when(objectMapper.readValue(eq(invalidJson), eq(GitHubResponse.class)))
                .thenAnswer(invocation -> { throw new IOException("Parsing failed"); });

        // when & then
        var result = sut.getPopularGithubRepos("Java", LocalDate.of(2025, 10, 31));

        assertTrue(result.isEmpty());
        verify(scoreService, never()).calculatePopularityScore(any());
    }
}
