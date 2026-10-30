package pl.eked.lab.banking.api.response;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import pl.eked.lab.banking.api.model.HttpResponse;

public final class HttpResponseWriter {
    private HttpResponseWriter() {
        /* This utility class should not be instantiated */
    }

    public static void write(HttpExchange exchange, HttpResponse response) throws IOException {

        Objects.requireNonNull(response,  "Response must not be null");
        Objects.requireNonNull(exchange, "Exchange must not be null");

        exchange.getResponseHeaders().set("Content-Type", response.contentType());
        var responseBytes = response.body().getBytes(StandardCharsets.UTF_8);

        if (responseBytes.length == 0) {
            exchange.sendResponseHeaders(response.statusCode(), -1);
            exchange.close();
            return;
        }

        exchange.sendResponseHeaders(response.statusCode(), responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
