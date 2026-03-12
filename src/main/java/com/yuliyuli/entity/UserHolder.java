package com.yuliyuli.entity;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户上下文持有器
 * 结束时需使用removeUser方法移除用户，避免内存泄漏
 */
@Slf4j
public class UserHolder {
  /**
   * 存储当前线程用户的登录状态信息
   */
  private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

  /**
   * 设置当前线程用户，仅在拦截器使用该set方法
   * @param user 用户
   */
  public static void setUser(User user) {
    USER_HOLDER.set(user);
  }

  /**
   * 获取当前线程用户
   * 可在任意业务逻辑中使用该方法获取当前线程用户
   * @return 用户
   */
  public static User getUser() {
    return USER_HOLDER.get();
  }

  /**
   * 判断当前线程是否持有用户
   * @return 是否持有用户
   */
  public static boolean isHoldingUser() {
    return USER_HOLDER.get() != null;
  }

  /**
   * 移除当前线程用户
   * 结束时需调用该方法移除用户，避免内存泄漏
   */
  public static void removeUser() {
    try {
      USER_HOLDER.remove();
    } catch (Exception e) {
      log.error("移除用户失败", e);
    }
  }
}
