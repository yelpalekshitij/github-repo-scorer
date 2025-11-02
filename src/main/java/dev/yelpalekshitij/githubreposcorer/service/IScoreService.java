package dev.yelpalekshitij.githubreposcorer.service;

import dev.yelpalekshitij.githubreposcorer.model.RepositoryInfo;

public interface IScoreService {

    double calculatePopularityScore(RepositoryInfo repo);
}
