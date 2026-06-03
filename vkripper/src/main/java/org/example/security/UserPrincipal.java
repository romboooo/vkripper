package org.example.security;

import lombok.Getter;

import java.security.Principal;

public class UserPrincipal implements Principal {
    @Getter
    private final Long id;
    private final String username;
    @Getter
    private final String role;

    public UserPrincipal(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    @Override
    public String getName() {
        return username;
    }

}