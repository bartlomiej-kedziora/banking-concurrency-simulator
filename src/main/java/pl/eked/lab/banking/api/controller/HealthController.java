package pl.eked.lab.banking.api.controller;

import java.util.List;
import pl.eked.lab.banking.api.model.ApiRequest;
import pl.eked.lab.banking.api.model.HttpMethod;
import pl.eked.lab.banking.api.model.HttpResponse;
import pl.eked.lab.banking.api.model.HttpRoute;

public class HealthController implements RestController {
    private static final String PATH = "/api/health";

    public HttpResponse getStatus(ApiRequest request) {
        return HttpResponse.ok("Server is up!");
    }

    @Override
    public List<HttpRoute> getRoutes() {
        return List.of(
            new HttpRoute(
                HttpMethod.GET,
                PATH,
                this::getStatus
            )
        );
    }
}
