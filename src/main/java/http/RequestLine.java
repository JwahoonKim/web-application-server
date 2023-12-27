package http;

import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class RequestLine {

    private static final int METHOD_INDEX = 0;
    private static final int PATH_INDEX = 1;

    private final HttpMethod method;
    private final String path;
    private Map<String, String> parameters = new HashMap<>();

    public RequestLine(String line) {
        String[] tokens = line.split(" ");
        method = HttpMethod.valueOf(tokens[METHOD_INDEX]);

        if (method.isPostMethod()) {
            this.path = tokens[PATH_INDEX];
            return;
        }

        int index = tokens[PATH_INDEX].indexOf("?");
        if (index == -1) {
            this.path = tokens[PATH_INDEX];
            return;
        }

        this.path = tokens[PATH_INDEX].substring(0, index);
        this.parameters = HttpRequestUtils.parseQueryString(tokens[PATH_INDEX].substring(index + 1));
    }


    public HttpMethod getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }
}
