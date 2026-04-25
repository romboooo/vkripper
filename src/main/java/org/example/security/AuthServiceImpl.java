package org.example.security;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.response.UserResponse;
import org.example.entity.Role;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JaasAuthenticator jaasAuthenticator;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }

    @Override
    public UserResponse register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Пользователь уже существует");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setBalance(BigDecimal.ZERO);
        user.setRole(Role.BUYER);
        User saved = userRepository.save(user);
        return UserResponse.fromUser(saved);
    }

    @Override
    public String login(String username, String password) {
        try {
            Subject subject = jaasAuthenticator.authenticate(username, password);

            UserPrincipal principal = subject.getPrincipals().stream()
                    .filter(p -> p instanceof UserPrincipal)
                    .map(p -> (UserPrincipal) p)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Principal не найден в Subject"));

            UserDetails userDetails = new CustomUserDetails(
                    principal.getId(),
                    principal.getName(),
                    "",
                    List.of(new SimpleGrantedAuthority(principal.getRole()))
            );

            return jwtService.generateToken(userDetails, principal.getId(), principal.getRole());
        } catch (LoginException e) {
            throw new RuntimeException("Ошибка аутентификации: " + e.getMessage(), e);
        }
    }

    @Override
    public void assignRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        try {
            user.setRole(role);
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Некорректная роль: " + role);
        }
    }
}