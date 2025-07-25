package com.back.global.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

// member에 맞춰서 수정 예정
public class SecurityUser extends User {
    @Getter
    private final Long id;
    @Getter
    private final String email;
    @Getter
    private final boolean isAdmin;

    public SecurityUser(
            Long id,
            String email,
            String name,
            String password,
            boolean isAdmin,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(name, password, authorities);
        this.id = id;
        this.email = email;
        this.isAdmin = isAdmin;
    }
}
