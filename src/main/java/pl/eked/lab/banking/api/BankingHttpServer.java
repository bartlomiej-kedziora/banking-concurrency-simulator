package pl.eked.lab.banking.api;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import pl.eked.lab.banking.api.routing.HttpRouter;

public class BankingHttpServer {
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int PORT = 8080;
    private static final int CONNECTION_POOL_SIZE = 50;

    private final HttpServer httpServer;
    private final ExecutorService executorService;

    public BankingHttpServer(HttpRouter httpRouter) throws IOException {
        this(httpRouter, PORT);
    }

    public BankingHttpServer(HttpRouter httpRouter, int port) throws IOException {
        this.httpServer = HttpServer.create();
        this.httpServer.bind(new InetSocketAddress(IP_ADDRESS, port),0);
        executorService = Executors.newFixedThreadPool(CONNECTION_POOL_SIZE);
        httpServer.setExecutor(executorService);
        httpServer.createContext("/", httpRouter);
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(1);
        executorService.shutdown();
    }

    public int getPort() {
        return httpServer.getAddress().getPort();
    }
}
