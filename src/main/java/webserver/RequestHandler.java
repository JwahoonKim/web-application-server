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

            log.debug(requestMethod);

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
                String line;

                while (true) {
                    line = reader.readLine();
                    if (line.startsWith("Content-Length")) {
                        break;
                    }
                }

                int contentLength = Integer.parseInt(line.split(":")[1].trim());

                while (true) {
                    line = reader.readLine();
                    if (line.equals("")) {
                        break;
                    }
                }

                String body = IOUtils.readData(reader, contentLength);

                Map<String, String> paramMap = HttpRequestUtils.parseQueryString(body);

                User user = new User(
                        paramMap.getOrDefault("userId", null),
                        paramMap.getOrDefault("password", null),
                        paramMap.getOrDefault("name", null),
                        paramMap.getOrDefault("email", null)
                );

                DataBase.addUser(user);
                response302Header(dos, "/index.html");
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
                String line;

                while (true) {
                    line = reader.readLine();
                    if (line.startsWith("Content-Length")) {
                        break;
                    }
                }

                int contentLength = Integer.parseInt(line.split(":")[1].trim());

                while (true) {
                    line = reader.readLine();
                    if (line.equals("")) {
                        break;
                    }
                }

                String body = IOUtils.readData(reader, contentLength);
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

            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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

    private void response302Header(DataOutputStream dos, String redirectUri) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
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
