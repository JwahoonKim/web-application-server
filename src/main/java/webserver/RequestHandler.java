package webserver;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            DataOutputStream dos = new DataOutputStream(out);

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String requestLine = reader.readLine();
            String path = requestLine.split(" ")[1];

            if (path.equals("/index.html")) {
                Path resourcePath = Paths.get("./webapp/index.html");
                byte[] body = Files.readAllBytes(resourcePath);
                response200Header(dos, body.length);
                responseBody(dos, body);
                return;
            }

            if (path.equals("/user/form.html")) {
                Path resourcePath = Paths.get("./webapp/user/form.html");
                byte[] body = Files.readAllBytes(resourcePath);
                response200Header(dos, body.length);
                responseBody(dos, body);
                return;
            }

            if (path.startsWith("/user/create")) {
                int index = path.indexOf('?');
                String params = path.substring(index + 1);
                Map<String, String> paramMap = HttpRequestUtils.parseQueryString(params);

                User user = new User(
                        paramMap.getOrDefault("userId", null),
                        paramMap.getOrDefault("password", null),
                        paramMap.getOrDefault("name", null),
                        paramMap.getOrDefault("email", null)
                );

                DataBase.addUser(user);
                return;
            }

            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
