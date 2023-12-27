package webserver;

import controller.*;
import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private RequestMapping requestMapping;

    public RequestHandler(Socket connectionSocket, RequestMapping requestMapping) {
        this.connection = connectionSocket;
        this.requestMapping = requestMapping;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            requestMapping.getController(request.getPath())
                    .service(request, response);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
