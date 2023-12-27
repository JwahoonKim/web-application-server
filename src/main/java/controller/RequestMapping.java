package controller;

import java.util.HashMap;
import java.util.Map;

public class RequestMapping {

    private static Map<String, Controller> controllers;
    private static DefaultController defaultController = new DefaultController();

    static {
        controllers = new HashMap<>();
        controllers.put("/user/create", new CreateUserController());
        controllers.put("/user/login", new LoginController());
        controllers.put("/user/list", new ListUserController());
    }

    public Controller getController(String requestUri) {
        return controllers.getOrDefault(requestUri, defaultController);
    }
}
