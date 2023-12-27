package controller;

import http.HttpRequest;
import http.HttpResponse;

public class ListUserController extends AbstractController {

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        if (request.isLogin()) {
            response.forward("/user/list.html");
            return;
        }

        response.forward("/user/login.html");
    }

}
