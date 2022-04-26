package com.cvent.dw.helloworld.auth;

import com.cvent.dw.helloworld.core.User;
import io.dropwizard.auth.Authorizer;

public class BasicDwAuthorizer implements Authorizer<User> {

    @Override
    public boolean authorize(User user, String role) {
        return user.getRoles() != null && user.getRoles().contains(role);
    }
}
