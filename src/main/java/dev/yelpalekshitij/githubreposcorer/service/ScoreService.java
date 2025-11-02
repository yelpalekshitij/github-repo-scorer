package dev.yelpalekshitij.githubreposcorer.service;

import dev.yelpalekshitij.githubreposcorer.config.AppConfigProperties;
import dev.yelpalekshitij.githubreposcorer.model.RepositoryInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ScoreService implements IScoreService {

    private final AppConfigProperties configProperties;

    @Override
    public double calculatePopularityScore(RepositoryInfo repo) {
        long stars = repo.stargazersCount();
        long forks = repo.forksCount();
        long ageDays = Math.max(1, (Instant.now().getEpochSecond() - repo.updatedAt().getEpochSecond()) / 86400);
        return (stars * configProperties.getPopularityScoreWeights().getStars() + forks * configProperties.getPopularityScoreWeights().getForks()) / Math.sqrt(ageDays);
    }
}
