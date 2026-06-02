package org.example.security;

import lombok.Setter;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.Map;

public class AppLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;
    private User userFromDb;
    private UserPrincipal userPrincipal;

    @Setter
    private static UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        try {
            NameCallback nameCallback = new NameCallback("Username: ");
            PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);
            callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});

            String username = nameCallback.getName();
            String password = new String(passwordCallback.getPassword());

            if (userRepository == null) {
                throw new LoginException("UserRepository не инициализирован в JAAS");
            }

            userFromDb = userRepository.findByUsername(username)
                    .orElseThrow(() -> new LoginException("Пользователь не найден: " + username));

            if (!passwordEncoder.matches(password, userFromDb.getPassword())) {
                throw new LoginException("Неверный пароль");
            }

            return true;
        } catch (Exception e) {
            throw new LoginException(e.getMessage());
        }
    }

    @Override
    public boolean commit() throws LoginException {
        if (userFromDb == null) return false;
        userPrincipal = new UserPrincipal(
                userFromDb.getId(),
                userFromDb.getUsername(),
                userFromDb.getRole().name()
        );
        subject.getPrincipals().add(userPrincipal);
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return false;
    }

    @Override
    public boolean logout() throws LoginException {
        if (userPrincipal != null) {
            subject.getPrincipals().remove(userPrincipal);
        }
        return true;
    }
}