package dev.yelpalekshitij.githubreposcorer.service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

interface IHttpCallerService {
    /**
     * Sends a GET request to the specified [uri].
     */
    HttpResponse<String> get(String uri, Map<String, String> headers, Map<String, String> params, Long timeout) throws IOException, InterruptedException;
}
