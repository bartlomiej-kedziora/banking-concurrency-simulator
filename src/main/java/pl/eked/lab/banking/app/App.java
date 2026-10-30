package pl.eked.lab.banking.app;

import java.io.IOException;
import pl.eked.lab.banking.config.ApplicationConfiguration;

public class App {

    static void main() throws IOException {
        var appConfig = new ApplicationConfiguration();
        var httpServer = appConfig.createServer();
        httpServer.start();
    }
}
