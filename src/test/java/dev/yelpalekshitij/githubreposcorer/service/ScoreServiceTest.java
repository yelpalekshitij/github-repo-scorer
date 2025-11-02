package dev.yelpalekshitij.githubreposcorer.service;

import dev.yelpalekshitij.githubreposcorer.config.AppConfigProperties;
import dev.yelpalekshitij.githubreposcorer.model.RepositoryInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreServiceTest {

    @Mock
    private AppConfigProperties configProperties;

    @InjectMocks
    private ScoreService sut;

    @Test
    void testCalculatePopularityScore() {
        // given
        AppConfigProperties.PopularityScoreWeightsProperties weights = new AppConfigProperties.PopularityScoreWeightsProperties();
        weights.setStars(0.6);
        weights.setForks(0.3);

        when(configProperties.getPopularityScoreWeights()).thenReturn(weights);

        RepositoryInfo repo = mock(RepositoryInfo.class);
        when(repo.stargazersCount()).thenReturn(100L);
        when(repo.forksCount()).thenReturn(50L);
        when(repo.updatedAt()).thenReturn(Instant.now().minusSeconds(86400)); // 1 day ago

        // when
        double score = sut.calculatePopularityScore(repo);

        // then
        assertTrue(score > 0, "Score should be positive");
    }
}
