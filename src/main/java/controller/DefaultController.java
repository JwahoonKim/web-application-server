package controller;

import http.HttpRequest;
import http.HttpResponse;

public class DefaultController extends AbstractController {
    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        response.forward(request.getPath());
    }
}
