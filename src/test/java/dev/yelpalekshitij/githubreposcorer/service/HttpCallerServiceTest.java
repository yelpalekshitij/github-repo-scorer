package dev.yelpalekshitij.githubreposcorer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpCallerServiceTest {

    @Mock
    private HttpClient mockClient;

    @Mock
    private HttpResponse<String> mockResponse;

    private HttpCallerService sut;

    @BeforeEach
    void setUp() {
        sut = new HttpCallerService(mockClient);
    }

    @Test
    void shouldReturnHttpResponse() throws IOException, InterruptedException {
        // given
        String uri = "https://example.com/api";
        Map<String, String> headers = Map.of("X-Test", "header-value");
        Map<String, String> params = Map.of("q", "java");
        long timeout = 5;

        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        // when
        HttpResponse<String> response = sut.get(uri, headers, params, timeout);

        // then
        assertSame(mockResponse, response);

        // Verify request was built correctly
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockClient).send(requestCaptor.capture(), any());

        HttpRequest captured = requestCaptor.getValue();
        assertEquals(URI.create("https://example.com/api?q=java"), captured.uri());
        assertEquals("header-value", captured.headers().firstValue("X-Test").orElse(null));
        assertEquals("GET", captured.method());
    }

    @Test
    void shouldThrowIOExceptionWhenClientFails() throws IOException, InterruptedException {
        // given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Connection failed"));

        // when/then
        assertThrows(IOException.class, () ->
                sut.get("https://example.com/api", null, null, null)
        );
    }

    @Test
    void shouldThrowInterruptedExceptionWhenClientInterrupted() throws IOException, InterruptedException {
        // given
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException("Interrupted"));

        // when/then
        assertThrows(InterruptedException.class, () ->
                sut.get("https://example.com/api", null, null, null)
        );
    }
}

