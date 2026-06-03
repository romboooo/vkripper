package org.example.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.function.Function;

public interface JwtService {
    String generateToken(UserDetails userDetails, Long userId, String role);
    String extractUsername(String token);
    Long extractUserId(String token);
    String extractRole(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    boolean validateToken(String token, UserDetails userDetails);
}
