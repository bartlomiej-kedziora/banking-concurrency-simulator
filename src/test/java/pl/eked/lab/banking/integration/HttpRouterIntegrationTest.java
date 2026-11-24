package pl.eked.lab.banking.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void tearDown() {
        if (server != null)
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

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(200, result.statusCode());
        }

        @Test
        void shouldReturnNotFoundForUnknownPath() throws IOException, InterruptedException {
            //given
            var requestedPath = "/api/v1/path";
            var registeredPath = "/api/v1/anotherpath";
            var route = new HttpRoute(
                HttpMethod.GET,
                registeredPath,
                _ -> HttpResponse.ok("OK")
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.creteGetRequestOnPath(requestedPath);

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(404, result.statusCode());
        }

        @Test
        void shouldReturnNotFoundForDifferentPathCase() throws IOException, InterruptedException {
            //given
            var requestedPath = "/api/v1/path";
            var registeredPath = "/api/v1/PATH";
            var route = new HttpRoute(
                HttpMethod.GET,
                registeredPath,
                _ -> HttpResponse.ok("OK")
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.creteGetRequestOnPath(requestedPath);

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(404, result.statusCode());
        }

        @Test
        void shouldDispatchRequestToCorrectHandler() throws IOException, InterruptedException {
            //given
            var requestedPath = "/api/v1/path";
            var route1 = new HttpRoute(
                HttpMethod.GET,
                requestedPath,
                _ -> HttpResponse.ok("OK GET")
            );
            var route2 = new HttpRoute(
                HttpMethod.POST,
                requestedPath,
                _ -> HttpResponse.ok("OK POST")
            );
            var routes = List.of(route1, route2);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.creteGetRequestOnPath(requestedPath);

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(200, result.statusCode());
            assertEquals("OK GET", result.body());
        }
    }

    @Nested
    class MethodHandling {
        @Test
        void shouldReturnMethodNotAllowedForUnsupportedMethod()
            throws IOException, InterruptedException {
            //given
            var requestedPath = "/api/v1/path";
            var route = new HttpRoute(
                HttpMethod.GET,
                requestedPath,
                _ -> HttpResponse.ok("OK GET")
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.cretePutRequestOnPath(requestedPath);

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(405, result.statusCode());
            assertEquals("", result.body());
        }

        @Test
        void shouldReturnMethodNotAllowedForUnknownHttpMethod()
            throws IOException, InterruptedException {
            //given
            var requestedPath = "/api/v1/path";
            var route = new HttpRoute(
                HttpMethod.GET,
                requestedPath,
                _ -> HttpResponse.ok("OK GET")
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.cretePatchRequestOnPath(requestedPath);

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(405, result.statusCode());
            assertEquals("", result.body());
        }

        @Test
        void shouldIncludeAllowHeader() throws IOException, InterruptedException {
            //given
            var requestedPath = "/api/v1/path";
            var route = new HttpRoute(
                HttpMethod.GET,
                requestedPath,
                _ -> HttpResponse.ok("OK GET")
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.cretePatchRequestOnPath(requestedPath);

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(405, result.statusCode());
            assertEquals("GET", result.headers().firstValue("Allow").orElse(""));
            assertEquals("", result.body());
        }
    }

    @Nested
    class RequestBody {
        @Test
        void shouldAcceptEmptyBody() throws IOException, InterruptedException {
            //given
            var requestedPath = "/api/v1/path";
            var route = new HttpRoute(
                HttpMethod.PUT,
                requestedPath,
                _ -> HttpResponse.status(200)
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.cretePutRequestOnPath(requestedPath);

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(200, result.statusCode());
            assertEquals("", result.body());
        }

        @Test
        void shouldPassRequestBodyToHandler() throws IOException, InterruptedException {
            //given
            var requestedPath = "/api/v1/path";
            var requestBody = "My request body";
            var route = new HttpRoute(
                HttpMethod.POST,
                requestedPath,
                req -> HttpResponse.ok(req.body())
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.cretePostRequestOnPath(requestedPath, requestBody);

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(200, result.statusCode());
            assertEquals(requestBody, result.body());
        }

        @Test
        void shouldPreserveUtf8Characters() throws IOException, InterruptedException {
            var requestedPath = "/api/v1/path";
            var requestBodyNoASCII = "Zażółć gęślą jaźń 日本語";
            var route = new HttpRoute(
                HttpMethod.POST,
                requestedPath,
                req -> HttpResponse.ok(req.body())
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.cretePostRequestOnPath(requestedPath, requestBodyNoASCII);

            //then
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(200, result.statusCode());
            assertEquals(requestBodyNoASCII, result.body());
        }
    }

    @Nested
    class RequestBodyLimit {
        @Test
        void shouldAcceptBodyBelowLimit() throws IOException, InterruptedException {
            //given
            var requestedPath = "/api/v1/path";
            var requestBodyBelowLimit = "A".repeat(Fixtures.MAX_REQUEST_BODY_SIZE - 1);
            var route = new HttpRoute(
                HttpMethod.POST,
                requestedPath,
                req -> HttpResponse.ok(req.body())
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.cretePostRequestOnPath(requestedPath, requestBodyBelowLimit);

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(200, result.statusCode());
        }

        @Test
        void shouldAcceptBodyAtLimit() throws IOException, InterruptedException {
            //given
            var requestedPath = "/api/v1/path";
            var requestBodyAtLimit = "A".repeat(Fixtures.MAX_REQUEST_BODY_SIZE);
            var route = new HttpRoute(
                HttpMethod.POST,
                requestedPath,
                req -> HttpResponse.ok(req.body())
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.cretePostRequestOnPath(requestedPath, requestBodyAtLimit);

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(200, result.statusCode());
        }

        @Test
        void shouldRejectBodyAboveLimit() throws IOException, InterruptedException {
            //given
            var requestedPath = "/api/v1/path";
            var requestBodyAboveLimit = "A".repeat(Fixtures.MAX_REQUEST_BODY_SIZE + 1);
            var route = new HttpRoute(
                HttpMethod.POST,
                requestedPath,
                req -> HttpResponse.ok(req.body())
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.cretePostRequestOnPath(requestedPath, requestBodyAboveLimit);

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(413, result.statusCode());
        }

        @Test
        void shouldRejectLargeBodyWithoutInvokingHandler() throws IOException, InterruptedException {
            //given
            var handlerInvoked = new AtomicBoolean(false);

            var requestedPath = "/api/v1/path";
            var requestBodyAboveLimit = "A".repeat(Fixtures.MAX_REQUEST_BODY_SIZE + 1);
            var route = new HttpRoute(
                HttpMethod.POST,
                requestedPath,
                _ -> {
                    handlerInvoked.set(true);
                    return HttpResponse.ok("OK POST");
                }
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.cretePostRequestOnPath(requestedPath, requestBodyAboveLimit);

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(413, result.statusCode());
            assertFalse(handlerInvoked.get());
        }
    }

    @Nested
    class ResponseHandling {
        @Test
        void shouldReturnHandlerResponseStatus() throws IOException, InterruptedException {
            //given
            var requestedPath = "/api/v1/path";
            var requestBody = "My request body";
            var route = new HttpRoute(
                HttpMethod.POST,
                requestedPath,
                _ -> HttpResponse.status(201)
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.cretePostRequestOnPath(requestedPath, requestBody);

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(201, result.statusCode());
        }

        @Test
        void shouldSetContentTypeHeader() throws IOException, InterruptedException {
            //given
            var requestedPath = "/api/v1/path";
            var requestBody = "My request body";
            var route = new HttpRoute(
                HttpMethod.POST,
                requestedPath,
                _ -> HttpResponse.ok("OK POST")
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.cretePostRequestOnPath(requestedPath, requestBody);

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(200, result.statusCode());
            assertEquals("text/plain; charset=UTF-8", result.headers().firstValue("Content-Type").orElseThrow());
        }

        @Test
        void shouldReturnEmptyBodyForErrorResponse() throws IOException, InterruptedException {
            //given
            var requestedPath = "/api/v1/path";
            var route = new HttpRoute(
                HttpMethod.GET,
                requestedPath,
                _ -> HttpResponse.status(401)
            );
            var routes = List.of(route);
            var givenControllers = List.of(Fixtures.givenController(routes));
            Fixtures.startServer(givenControllers);
            var httpRequest = Fixtures.creteGetRequestOnPath(requestedPath);

            //when
            var result = client.send(httpRequest, BodyHandlers.ofString());

            //then
            assertEquals(401, result.statusCode());
            assertTrue(result.body().isEmpty());
        }
    }

    private static class Fixtures {
        private static final int MAX_REQUEST_BODY_SIZE = 64 * 1024; // 64 KiB

        private static void startServer(List<RestController> controllers) throws IOException {
            HttpRouter httpRouter = new HttpRouter(controllers);
            server = new BankingHttpServer(httpRouter, 0);
            server.start();
        }

        private static HttpRequest creteGetRequestOnPath(String path) {
            return HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(5))
                .GET()
                .uri(URI.create("http://127.0.0.1:" + server.getPort() + path))
                .build();
        }

        private static HttpRequest cretePatchRequestOnPath(String path) {
            return HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(5))
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .uri(URI.create("http://127.0.0.1:" + server.getPort() + path))
                .build();
        }

        private static HttpRequest cretePutRequestOnPath(String path) {
            return HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(5))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create("http://127.0.0.1:" + server.getPort() + path))
                .build();
        }

        private static HttpRequest cretePostRequestOnPath(String path, String body) {
            return HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofString(body))
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
