package pl.eked.lab.banking.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.eked.lab.banking.api.BankingHttpServer;
import pl.eked.lab.banking.api.controller.HealthController;
import pl.eked.lab.banking.api.controller.RestController;

class HealthControllerIntegrationTest {
    private static BankingHttpServer server;
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() throws IOException {
        List<RestController> controllers = List.of(
            new HealthController()
        );
        server = new BankingHttpServer(controllers, 0);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void shouldReturn200WhenHealthEndpointIsCalledWithGet()
        throws IOException, InterruptedException {
        //given
        var httpRequest = HttpRequest.newBuilder()
            .timeout(Duration.ofSeconds(5))
            .GET()
            .uri(URI.create("http://127.0.0.1:" + server.getPort() + "/api/health"))
            .build();

        var expectedResponse = "Server is up!";

        //when
        var result = client.send(httpRequest, BodyHandlers.ofString());

        //then
        assertEquals(200, result.statusCode());
        assertEquals(
            "text/plain; charset=UTF-8",
            result.headers().firstValue("Content-Type").orElseThrow()
        );
        assertEquals(expectedResponse, result.body());
    }

    @Test
    void shouldReturn405WhenHealthEndpointIsCalledWithPost()
        throws IOException, InterruptedException {
        //given
        var httpRequest = HttpRequest.newBuilder()
            .timeout(Duration.ofSeconds(5))
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create("http://127.0.0.1:" + server.getPort() + "/api/health"))
            .build();

        var expectedResponse = "";
        var expectedResponseHeader = "GET";

        //when
        var result = client.send(httpRequest, BodyHandlers.ofString());

        //then
        assertEquals(405, result.statusCode());
        assertEquals(
            "text/plain; charset=UTF-8",
            result.headers().firstValue("Content-Type").orElseThrow()
        );
        assertEquals(expectedResponseHeader, result.headers().firstValue("Allow").orElseThrow());
        assertEquals(expectedResponse, result.body());

    }
}
