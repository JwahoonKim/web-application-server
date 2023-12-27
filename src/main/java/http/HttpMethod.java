package http;

public enum HttpMethod {
    GET, POST;

    public boolean isPostMethod() {
        return this == POST;
    }

    public boolean isGetMethod() {
        return this == GET;
    }
}
