package pl.eked.lab.banking.config;

import java.io.IOException;
import java.util.List;
import pl.eked.lab.banking.api.BankingHttpServer;
import pl.eked.lab.banking.api.controller.HealthController;
import pl.eked.lab.banking.api.controller.RestController;

public final class ApplicationConfiguration {

    public BankingHttpServer createServer() throws IOException {
        return new BankingHttpServer(this.createControllers());
    }

    private List<RestController> createControllers() {
        return List.of(
            new HealthController()
        );
    }
}
