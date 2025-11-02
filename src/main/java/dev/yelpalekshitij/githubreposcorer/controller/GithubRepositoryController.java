package dev.yelpalekshitij.githubreposcorer.controller;

import dev.yelpalekshitij.githubreposcorer.model.RepositoryDTO;
import dev.yelpalekshitij.githubreposcorer.service.IGithubRepoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/repositories")
public class GithubRepositoryController {

    private final IGithubRepoService repositoryService;

    public GithubRepositoryController(IGithubRepoService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @GetMapping("/search")
    public List<RepositoryDTO> search(
            @RequestParam
            String language,

            @RequestParam("earliestDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate earliestDate
    ) {
        return repositoryService.getPopularGithubRepos(language, earliestDate);
    }
}
