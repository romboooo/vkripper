package org.example.security;

import org.springframework.stereotype.Service;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

@Service
public class JaasAuthenticator {

    public Subject authenticate(String username, String password) throws LoginException {
        LoginContext loginContext = new LoginContext(
                "AppLogin",
                new UsernamePasswordCallbackHandler(username, password)
        );
        loginContext.login();
        return loginContext.getSubject();
    }

    private record UsernamePasswordCallbackHandler(String username, String password) implements CallbackHandler {

        @Override
        public void handle(Callback[] callbacks) {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(username);
                } else if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(password.toCharArray());
                }
            }
        }
    }
}