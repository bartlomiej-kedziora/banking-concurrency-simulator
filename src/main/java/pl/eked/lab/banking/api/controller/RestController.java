package pl.eked.lab.banking.api.controller;

import java.util.List;
import pl.eked.lab.banking.api.model.HttpRoute;

public interface RestController {
    List<HttpRoute> getRoutes();
}
