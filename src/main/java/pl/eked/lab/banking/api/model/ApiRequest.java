package pl.eked.lab.banking.api.model;

import java.util.Map;

public record ApiRequest(
    String body,
    Map<String, String> queryParameters
) {}
