package pl.eked.lab.banking.api.routing;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import pl.eked.lab.banking.api.controller.RestController;
import pl.eked.lab.banking.api.model.ApiRequest;
import pl.eked.lab.banking.api.model.HttpMethod;
import pl.eked.lab.banking.api.model.HttpResponse;
import pl.eked.lab.banking.api.model.HttpRoute;
import pl.eked.lab.banking.api.response.HttpResponseWriter;

public class HttpRouter implements HttpHandler {
    private static final int MAX_REQUEST_BODY_SIZE = 64 * 1024; // 64 KiB
    private final Map<String, Map<HttpMethod, HttpRoute>> routes;

    public HttpRouter(List<RestController> controllers) {
        final Map<String, Map<HttpMethod, HttpRoute>> localRoutes = new HashMap<>();
        for (RestController controller : controllers) {
            var httpRoutes = controller.getRoutes();
            for (HttpRoute route : httpRoutes) {
                var routesByMethod = localRoutes.computeIfAbsent(route.path(), k -> new HashMap<>());
                var existingRoute = routesByMethod.putIfAbsent(route.method(), route);
                if (existingRoute != null) {
                    throw new IllegalStateException(
                        "Duplicate route found for " + route.method() + " " + route.path());
                }
            }
        }

        localRoutes.replaceAll((path, routesByMethod)
            -> Map.copyOf(routesByMethod)
        );
        this.routes = Map.copyOf(localRoutes);
    }

    public void handle(HttpExchange exchange) throws IOException {
        var requestPath = exchange.getRequestURI().getPath();
        var existingPath = routes.get(requestPath);
        if (existingPath == null) {
            this.writePathNotFoundResponse(exchange);
            return;
        }

        if (!HttpMethod.isRequestMethodHandled(exchange.getRequestMethod())) {
            this.writeMethodNotAllowedResponse(existingPath, exchange);
            return;
        }

        var requestMethod = HttpMethod.valueOf(exchange.getRequestMethod());
        var route = existingPath.get(requestMethod);
        if (route == null) {
            this.writeMethodNotAllowedResponse(existingPath, exchange);
            return;
        }

        var requestBytes = exchange.getRequestBody().readNBytes(MAX_REQUEST_BODY_SIZE + 1);
        if (requestBytes.length > MAX_REQUEST_BODY_SIZE) {
            this.writeContentTooLargeResponse(exchange);
            return;
        }

        var requestBody = new String(
            exchange.getRequestBody().readAllBytes(),
            StandardCharsets.UTF_8
        );
//        var requestParams = exchange.getRequestURI().getRawQuery();
        var apiRequest  = new ApiRequest(requestBody, Map.of());

        var response = route.handler().apply(apiRequest);

        HttpResponseWriter.write(exchange, response);
    }

    private void writeMethodNotAllowedResponse(
        Map<HttpMethod, HttpRoute> existingPath,
        HttpExchange exchange
    ) throws IOException {
        var allowedHttpMethod = existingPath.keySet().stream()
            .map(Enum::name)
            .collect(Collectors.joining(", "));
        var response = HttpResponse.status(405);
        exchange.getResponseHeaders().set("Allow", allowedHttpMethod);
        HttpResponseWriter.write(exchange, response);
    }

    private void writePathNotFoundResponse(HttpExchange exchange) throws IOException {
        var response = HttpResponse.status(404);
        HttpResponseWriter.write(exchange, response);
    }

    private void writeContentTooLargeResponse(HttpExchange exchange) throws IOException {
        var response = HttpResponse.status(413);
        HttpResponseWriter.write(exchange, response);
    }
}
