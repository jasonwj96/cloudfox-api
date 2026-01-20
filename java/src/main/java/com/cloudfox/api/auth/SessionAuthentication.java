package com.cloudfox.api.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.UUID;

public class SessionAuthentication extends AbstractAuthenticationToken {

    private final UUID accountId;

    public SessionAuthentication(UUID accountId) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.accountId = accountId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return accountId;
    }
}