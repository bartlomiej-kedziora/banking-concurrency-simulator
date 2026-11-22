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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pl.eked.lab.banking.api.BankingHttpServer;
import pl.eked.lab.banking.api.controller.RestController;
import pl.eked.lab.banking.api.model.HttpMethod;
import pl.eked.lab.banking.api.model.HttpResponse;
import pl.eked.lab.banking.api.model.HttpRoute;
import pl.eked.lab.banking.api.routing.HttpRouter;

class HttpRouterIntegrationTest {
    private static BankingHttpServer server;
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() {
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Nested
    class RouteMatching {
        @Test
        void shouldReturnOkForRegisteredRoute() throws IOException, InterruptedException {
            //given
            var requestedPath = "/api/v1/path";
            var route = new HttpRoute(
                HttpMethod.GET,
                requestedPath,
                _ -> HttpResponse.ok("OK")
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.creteGetRequestOnPath(requestedPath);

            //then
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(200, result.statusCode());
        }

        @Test
        void shouldReturnNotFoundForUnknownPath() {}

        @Test
        void shouldReturnNotFoundForDifferentPathCase() {}

        @Test
        void shouldDispatchRequestToCorrectHandler() {}
    }

    @Nested
    class MethodHandling {
        @Test
        void shouldReturnMethodNotAllowedForUnsupportedMethod() {}

        @Test
        void shouldReturnMethodNotAllowedForUnknownHttpMethod() {}

        @Test
        void shouldIncludeAllowHeader() {}
    }

    @Nested
    class RequestBody {
        @Test
        void shouldAcceptEmptyBody() {}

        @Test
        void shouldPassRequestBodyToHandler() {}

        @Test
        void shouldPreserveUtf8Characters() {}
    }

    @Nested
    class RequestBodyLimit {
        @Test
        void shouldAcceptBodyBelowLimit() {}

        @Test
        void shouldAcceptBodyAtLimit() {}

        @Test
        void shouldRejectBodyAboveLimit() {}

        @Test
        void shouldRejectLargeBodyWithoutInvokingHandler() {}
    }

    @Nested
    class ResponseHandling {
        @Test
        void shouldReturnHandlerResponseStatus() {}

        @Test
        void shouldReturnHandlerResponseBody() {}

        @Test
        void shouldSetContentTypeHeader() {}

        @Test
        void shouldReturnEmptyBodyForErrorResponse() {}
    }

    private static class Fixtures {
        private static void startServer(List<RestController> controllers) {
            HttpRouter httpRouter = new HttpRouter(controllers);
            try {
                server = new BankingHttpServer(httpRouter, 0);
                server.start();
            } catch (IOException ex) {
                System.out.println("Server not started, because of: " + ex.getMessage());
            }
            finally {
                server.stop();
            }
        }

        private static HttpRequest creteGetRequestOnPath(String path) {
            return HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(5))
                .GET()
                .uri(URI.create("http://127.0.0.1:" + server.getPort() + path))
                .build();
        }

        private static RestController givenController(HttpRoute route) {
            return () -> List.of(route);
        }

        private static RestController givenController(List<HttpRoute> routes) {
            return () -> routes;
        }
    }
}
