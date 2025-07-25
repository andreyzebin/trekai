package info.jtrac.config.jwt;

import org.springframework.security.core.Authentication;

public interface TokenProvider {
    String generateToken(Authentication authentication);

    String getUsernameFromJWT(String token);

    boolean validateToken(String authToken);
}
