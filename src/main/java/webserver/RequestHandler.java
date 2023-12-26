package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

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
            String[] split = requestLine.split(" ");
            String requestMethod = split[0];
            String path = split[1];

            log.debug("path = {}", path);

            // request를 읽어서 헤더값 map으로 변환하기
            String headerString = getHeaderString(reader);
            Map<String, String> headerMap = HttpRequestUtils.parseRequestHeaderString(headerString);
            log.debug("headerMap : {}", headerMap);

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

            if (path.startsWith("/user/create") && requestMethod.equals("GET")) {
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

            if (path.startsWith("/user/create") && requestMethod.equals("POST")) {
                int contentLength = Integer.parseInt(headerMap.get("Content-Length"));
                String body = IOUtils.readData(reader, contentLength);
                log.debug("body : {}", body);

                Map<String, String> paramMap = HttpRequestUtils.parseQueryString(body);

                User user = new User(
                        paramMap.getOrDefault("userId", null),
                        paramMap.getOrDefault("password", null),
                        paramMap.getOrDefault("name", null),
                        paramMap.getOrDefault("email", null)
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
                int contentLength = Integer.parseInt(headerMap.get("Content-Length"));
                String body = IOUtils.readData(reader, contentLength);

                log.debug("body : {}", body);

                Map<String, String> paramMap = HttpRequestUtils.parseQueryString(body);
                User user = DataBase.findUserById(paramMap.getOrDefault("userId", null));

                // 로그인 실패시 login_failed.html로 리다이렉트
                if (user == null) {
                    response302HeaderWithLoginFailed(dos, "/user/login_failed.html");
                    return;
                }

                if (!paramMap.get("password").equals(user.getPassword())) {
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
                // 쿠키 확인
                String cookies = headerMap.get("Cookie");
                Map<String, String> cookieMap = HttpRequestUtils.parseCookies(cookies);

                if (Boolean.parseBoolean(cookieMap.get("logined"))) {
                    Collection<User> users = DataBase.findAll();
                    // user 정보를 html로 변환
                    String body = getUserInfoBody(users);
                    response200Header(dos, body.length());
                    responseBody(dos, body.getBytes());
                    return;
                }

                response302Header(dos, "/user/login.html");
                return;
            }

            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
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

    private static String getHeaderString(BufferedReader reader) throws IOException {
        StringBuilder headerStringBuilder = new StringBuilder();
        while (true) {
            String line = reader.readLine();
            if (line == null || line.isEmpty()) {
                break;
            }
            headerStringBuilder.append(line).append("\n");
        }
        String headerString = headerStringBuilder.toString();
        return headerString;
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
