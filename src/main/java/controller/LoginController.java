package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.RequestHandler;

public class LoginController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
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
    }
}
