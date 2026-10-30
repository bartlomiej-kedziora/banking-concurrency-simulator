package pl.eked.lab.banking.api.controller;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import pl.eked.lab.banking.api.model.HttpMethod;
import pl.eked.lab.banking.api.model.HttpResponse;
import pl.eked.lab.banking.api.response.HttpResponseWriter;

public class HealthController implements RestController {

    @Override
    public String getPath() {
        return "/api/health";
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var allowedHttpMethod = HttpMethod.GET.name();
        if (!allowedHttpMethod.equals(exchange.getRequestMethod())) {
            var response = HttpResponse.status(405);
            exchange.getResponseHeaders().set("Allow", allowedHttpMethod);
            HttpResponseWriter.write(exchange, response);
            return;
        }

        var response = HttpResponse.ok("Server is up!");

        HttpResponseWriter.write(exchange, response);
    }
}
