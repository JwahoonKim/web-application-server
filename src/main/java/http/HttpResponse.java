package http;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.RequestHandler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class HttpResponse {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    public static final String BASE_PATH = "./webapp";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String TEXT_HTML_CHARSET_UTF_8 = "text/html;charset=utf-8";
    public static final String LOCATION = "Location";
    public static final String INDEX_HTML = "/index.html";
    public static final String SET_COOKIE = "Set-Cookie";
    public static final String TEXT_CSS = "text/css";
    public static final String APPLICATION_JAVASCRIPT = "application/javascript";
    public static final String HTTP_1_1 = "HTTP/1.1";

    private final DataOutputStream dos;

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void forward(String uri) {

        byte[] body = new byte[0];
        try {
            Path resourcePath = Paths.get(BASE_PATH + uri);
            body = Files.readAllBytes(resourcePath);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        addHeaderOfStatus(StatusCode.OK);
        addHeaderOfContentType(uri);
        addHeader(CONTENT_LENGTH, String.valueOf(body.length));
        addEndOfHeader();
        addBody(body);
    }

    private void addHeaderOfContentType(String uri) {
        if (uri.endsWith(".css")) {
            addHeader(CONTENT_TYPE, TEXT_CSS);
            return;
        }

        if (uri.endsWith(".js")) {
            addHeader(CONTENT_TYPE, APPLICATION_JAVASCRIPT);
            return;
        }

        addHeader(CONTENT_TYPE, TEXT_HTML_CHARSET_UTF_8);
    }

    public void sendRedirect(String redirectUri, Boolean logined) {
        addHeaderOfStatus(StatusCode.REDIRECT);
        addHeader(LOCATION, redirectUri);

        if (logined != null) {
            addHeader(SET_COOKIE, "logined=" + logined + "; Path=/");
        }

        addEndOfHeader();
    }

    public void addHeader(String key, String value) {
        try {
            dos.writeBytes(key + ": " + value + "\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void addBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void addHeaderOfStatus(StatusCode statusCode) {
        try {
            dos.writeBytes(HTTP_1_1 + " " + statusCode.getCode() + " " + statusCode.getMessage() + "\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void addEndOfHeader() {
        try {
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }


}
