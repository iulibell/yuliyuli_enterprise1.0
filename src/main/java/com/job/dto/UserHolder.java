package com.job.dto;

public class UserHolder {
    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    public static void setUser(User user) {
        USER_HOLDER.set(user);
    }

    public static User getUser() {
        return USER_HOLDER.get();
    }

    public static void removeUser() {
        USER_HOLDER.remove();
    }
}
