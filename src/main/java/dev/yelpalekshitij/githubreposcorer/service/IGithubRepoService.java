package dev.yelpalekshitij.githubreposcorer.service;

import dev.yelpalekshitij.githubreposcorer.model.RepositoryDTO;
import java.time.LocalDate;
import java.util.List;

public interface IGithubRepoService {

    List<RepositoryDTO> getPopularGithubRepos(String language, LocalDate date);
}
