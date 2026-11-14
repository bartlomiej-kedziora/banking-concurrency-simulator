package pl.eked.lab.banking.api.model;

import java.util.Arrays;
import java.util.Locale;

public enum HttpMethod {
    GET, POST, PUT, DELETE;

    public static boolean isRequestMethodHandled(String requestMethod) {
        return Arrays.stream(values())
            .anyMatch(httpMethod ->
                httpMethod.name().equals(requestMethod.toUpperCase(Locale.ROOT))
        );
    }
}
