package dev.yelpalekshitij.githubreposcorer.controller;

import dev.yelpalekshitij.githubreposcorer.model.RepositoryDTO;
import dev.yelpalekshitij.githubreposcorer.service.GithubRepoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GithubRepositoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GithubRepoService repoService;

    @Test
    void testSearchRepositories() throws Exception {
        // given
        RepositoryDTO dto = new RepositoryDTO("repo1","Java",10,5, Instant.now(), 50.0);
        when(repoService.getPopularGithubRepos(anyString(), any())).thenReturn(List.of(dto));

        // when/then
        mockMvc.perform(get("/api/repositories/search")
                        .param("language", "Java")
                        .param("earliestDate", "2025-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("repo1"))
                .andExpect(jsonPath("$[0].popularityScore").value(50.0));
    }
}
