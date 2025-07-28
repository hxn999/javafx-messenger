package com.client.util;

import com.db.User;

import java.util.List;

public class FindUser {
    public static User findUserByPhone(List<User> users, String phone) {
        for (User user : users) {
            if (user.getPhone().equals(phone)) {
                return user;
            }
        }
        return null;
    }
}
