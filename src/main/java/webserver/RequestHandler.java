package webserver;

import db.DataBase;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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

            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            HttpMethod requestMethod = request.getMethod();
            String path = request.getPath();

            log.debug("path = {}", path);

            if (path.equals("/index.html")) {
                response.forward("/index.html");
                return;
            }

            if (path.equals("/user/form.html")) {
                response.forward("/user/form.html");
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
                response.sendRedirect("/index.html", null);
                log.debug("user : {}, 저장 완료", user);
                return;
            }

            if (path.equals("/user/login.html")) {
                response.forward("/user/login.html");
                return;
            }

            if (path.equals("/user/login")) {
                User user = DataBase.findUserById(request.getParameter("userId"));

                // 로그인 실패시 login_failed.html로 리다이렉트
                if (user == null) {
                    response.sendRedirect("/user/login_failed.html", Boolean.FALSE);
                    return;
                }

                if (!request.getParameter("password").equals(user.getPassword())) {
                    response.sendRedirect("/user/login_failed.html", Boolean.FALSE);
                    return;
                }

                // 로그인 성공시
                log.debug("로그인 성공");
                response.sendRedirect("/index.html", Boolean.TRUE);
                return;
            }

            if (path.equals("/user/login_failed.html")) {
                response.forward("/user/login_failed.html");
                return;
            }

            if (path.equals("/user/list.html")) {
                if (request.isLogin()) {
                    response.forward("/user/list.html");
                    return;
                }

                response.forward("/user/login.html");
                return;
            }

            response.forward(path);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
