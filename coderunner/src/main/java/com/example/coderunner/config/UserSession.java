package com.example.coderunner.config;

public class UserSession {

    private static Long currentUserId;

    public static void setUserId(Long userId) {
        currentUserId = userId;
    }

    public static Long getUserId() {
        return currentUserId;
    }
}