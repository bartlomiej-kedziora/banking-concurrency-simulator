package pl.eked.lab.banking.api.controller;

import com.sun.net.httpserver.HttpHandler;

public interface RestController extends HttpHandler {
    String getPath();
}
