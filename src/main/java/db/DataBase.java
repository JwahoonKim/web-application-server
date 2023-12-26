package db;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import model.User;

public class DataBase {
    private static Map<String, User> users = Maps.newHashMap();

    public static void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static Collection<User> findAll() {
        return users.values();
    }

    public static void insertDefaultUsers() {
        DataBase.addUser(new User("crong", "1234", "crong", "crong@naver.com"));
        DataBase.addUser(new User("pobi", "1234", "pobi", "pobi@naver.com"));
        DataBase.addUser(new User("jk", "1234", "jk", "jk@naver.com"));
        DataBase.addUser(new User("123", "123", "kim", "kim@naver.com"));
    }
}
