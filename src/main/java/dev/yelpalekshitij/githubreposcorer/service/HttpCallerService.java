package dev.yelpalekshitij.githubreposcorer.service;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class HttpCallerService implements IHttpCallerService {

    private final HttpClient client;

    public HttpCallerService() {
        this(HttpClient.newBuilder().build());
    }

    HttpCallerService(HttpClient client) {
        this.client = client;
    }

    @Override
    public HttpResponse<String> get(
            String uri,
            Map<String, String> headers,
            Map<String, String> params,
            Long timeoutSeconds
    ) throws IOException, InterruptedException {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(uri);
        if (params != null) {
            params.forEach(uriBuilder::queryParam);
        }
        URI finalUri = uriBuilder.build().encode().toUri();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(finalUri)
                .GET();

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }

        if (timeoutSeconds != null) {
            requestBuilder.timeout(Duration.ofSeconds(timeoutSeconds));
        }

        return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
