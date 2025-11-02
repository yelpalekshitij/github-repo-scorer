package dev.yelpalekshitij.githubreposcorer.exception;

import lombok.Getter;

@Getter
public class GitHubApiException extends BaseException {

    private final int statusCode;
    private final String responseBody;

    public GitHubApiException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
}
