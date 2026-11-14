package pl.eked.lab.banking.config;

import java.io.IOException;
import java.util.List;
import pl.eked.lab.banking.api.BankingHttpServer;
import pl.eked.lab.banking.api.controller.HealthController;
import pl.eked.lab.banking.api.controller.RestController;
import pl.eked.lab.banking.api.routing.HttpRouter;

public final class ApplicationConfiguration {

    public BankingHttpServer createServer() throws IOException {
        return new BankingHttpServer(new HttpRouter(this.createControllers()));
    }

    private List<RestController> createControllers() {
        return List.of(
            new HealthController()
        );
    }
}
