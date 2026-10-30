package pl.eked.lab.banking.api.model;

public record HttpResponse(
    int statusCode,
    String body,
    String contentType
) {
    private static final String DEFAULT_CONTENT_TYPE = "text/plain; charset=UTF-8";

    public static HttpResponse ok(String body) {
        return new HttpResponse(200, body, DEFAULT_CONTENT_TYPE);
    }

    public static HttpResponse created(String body) {
        return new HttpResponse(201, body, DEFAULT_CONTENT_TYPE);
    }

    public static HttpResponse status(int statusCode) {
        return new HttpResponse(statusCode, "", DEFAULT_CONTENT_TYPE);
    }
}
