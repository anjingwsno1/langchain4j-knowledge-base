package com.anjingwsno1.langchain4jknowledgebase.common.base;

import com.anjingwsno1.langchain4jknowledgebase.entity.User;

public class MockUser {

    public static User getCurrentUser() {
        //模拟用户登录和上下文
        User user = new User();
        user.setId(1L);
        user.setUuid("UUID-User-001");
        user.setName("天涯兰");
        ThreadContext.setCurrentUser(user);

        return user;
    }
}
