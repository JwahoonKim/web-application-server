package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private static final String CONTENT_LENGTH = "Content-Length";

    private RequestLine requestLine;

    private final Map<String, String> headers = new HashMap<>();
    private Map<String, String> parameters = new HashMap<>();

    public HttpRequest(InputStream in) {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        try {
            String requestLine = br.readLine();
            this.requestLine = new RequestLine(requestLine);

            while (true) {
                String line = br.readLine();

                if (line == null || line.equals("")) {
                    break;
                }

                log.debug("header : {}", line);
                String[] split = line.split(":");
                headers.put(split[0].trim(), split[1].trim());
            }

            if (this.getMethod().isPostMethod()) {
                String body = IOUtils.readData(br, Integer.parseInt(headers.get(CONTENT_LENGTH)));
                parameters = HttpRequestUtils.parseQueryString(body);
                return;
            }

            parameters = this.requestLine.getParameters();

        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    public HttpMethod getMethod() {
        return requestLine.getMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getHeader(String headerName) {
        return headers.getOrDefault(headerName, null);
    }

    public String getParameter(String parameterName) {
        return parameters.getOrDefault(parameterName, null);
    }

    public boolean isLogin() {
        String logined = this.parameters.getOrDefault("logined", null);
        return Boolean.parseBoolean(logined);
    }
}
