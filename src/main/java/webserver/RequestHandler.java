package webserver;

import db.DataBase;
import http.HttpMethod;
import http.HttpRequest;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            DataOutputStream dos = new DataOutputStream(out);

            HttpRequest request = new HttpRequest(in);

            HttpMethod requestMethod = request.getMethod();
            String path = request.getPath();

            log.debug("path = {}", path);

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

            if (path.equals("/user/create") && requestMethod.isGetMethod()) {
                User user = new User(
                        request.getHeader("userId"),
                        request.getHeader("password"),
                        request.getHeader("name"),
                        request.getHeader("email")
                );

                DataBase.addUser(user);
                return;
            }

            if (path.equals("/user/create") && requestMethod.isPostMethod()) {
                User user = new User(
                        request.getHeader("userId"),
                        request.getHeader("password"),
                        request.getHeader("name"),
                        request.getHeader("email")
                );

                DataBase.addUser(user);
                response302Header(dos, "/index.html");
                log.debug("user : {}, 저장 완료", user);
                return;
            }

            if (path.equals("/user/login.html")) {
                Path resourcePath = Paths.get("./webapp/user/login.html");
                byte[] body = Files.readAllBytes(resourcePath);
                response200Header(dos, body.length);
                responseBody(dos, body);
                return;
            }

            if (path.equals("/user/login")) {
                User user = DataBase.findUserById(request.getParameter("userId"));

                // 로그인 실패시 login_failed.html로 리다이렉트
                if (user == null) {
                    response302HeaderWithLoginFailed(dos, "/user/login_failed.html");
                    return;
                }

                if (!request.getParameter("password").equals(user.getPassword())) {
                    response302HeaderWithLoginFailed(dos, "/user/login_failed.html");
                    return;
                }

                // 로그인 성공시
                log.debug("로그인 성공");
                response302HeaderWithLoginSuccess(dos, "/index.html");
                return;
            }

            if (path.equals("/user/login_failed.html")) {
                Path resourcePath = Paths.get("./webapp/user/login_failed.html");
                byte[] body = Files.readAllBytes(resourcePath);
                response200Header(dos, body.length);
                responseBody(dos, body);
                return;
            }

            if (path.equals("/user/list")) {
                if (request.isLogin()) {
                    Collection<User> users = DataBase.findAll();
                    String body = getUserInfoBody(users);
                    response200Header(dos, body.length());
                    responseBody(dos, body.getBytes());
                    return;
                }

                response302Header(dos, "/user/login.html");
                return;
            }

            if (path.equals("/css/styles.css")) {
                Path resourcePath = Paths.get("./webapp/css/styles.css");
                byte[] body = Files.readAllBytes(resourcePath);
                response200CssHeader(dos, body.length);
                responseBody(dos, body);
                return;
            }

            if (path.equals("/css/bootstrap.min.css")) {
                Path resourcePath = Paths.get("./webapp/css/bootstrap.min.css");
                byte[] body = Files.readAllBytes(resourcePath);
                response200CssHeader(dos, body.length);
                responseBody(dos, body);
                return;
            }

            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200CssHeader(DataOutputStream dos, int length) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + length + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private static String getUserInfoBody(Collection<User> users) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'>");
        for (User user : users) {
            sb.append("<tr>");
            sb.append("<td>" + user.getUserId() + "</td>");
            sb.append("<td>" + user.getName() + "</td>");
            sb.append("<td>" + user.getEmail() + "</td>");
            sb.append("</tr>");
        }
        return sb.toString();
    }

    private void response302HeaderWithLoginFailed(DataOutputStream dos, String redirectUri) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Set-Cookie: logined=false; Path=/\r\n");
            dos.writeBytes("Location: " + redirectUri + "\r\n"); // 리다이렉트할 URL을 여기에 넣으세요
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302HeaderWithLoginSuccess(DataOutputStream dos, String redirectUri) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Set-Cookie: logined=true; Path=/\r\n");
            dos.writeBytes("Location: " + redirectUri + "\r\n"); // 리다이렉트할 URL을 여기에 넣으세요
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String redirectUri) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Location: " + redirectUri + "\r\n"); // 리다이렉트할 URL을 여기에 넣으세요
            dos.writeBytes("\r\n");
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
