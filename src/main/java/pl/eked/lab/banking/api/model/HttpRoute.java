package pl.eked.lab.banking.api.model;

import java.util.function.Function;

public record HttpRoute(
    HttpMethod method,
    String path,
    Function<ApiRequest, HttpResponse> handler
){}
