package me.gnevilkoko.project_manager.models.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.gnevilkoko.project_manager.models.entities.BearerToken;
import me.gnevilkoko.project_manager.models.entities.User;
import me.gnevilkoko.project_manager.models.exceptions.TokenNotFoundException;
import me.gnevilkoko.project_manager.models.repositories.BearerTokenRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class BearerTokenService {
    private BearerTokenRepo repo;

    private final SecretKey key;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;
    private static final Logger logger = LoggerFactory.getLogger(BearerTokenService.class);

    @Autowired
    public BearerTokenService(
            BearerTokenRepo repo,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access.expiration}") long accessTokenValidity,
            @Value("${jwt.refresh.expiration}") long refreshTokenValidity
    ) {
        this.repo = repo;
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public UserDetailsService getUserDetailsService() {
        return token -> {
            String checkedToken = token;
            if(checkedToken.startsWith("Bearer")) {
                checkedToken = checkedToken.substring(7);
            }
            return repo.findByToken(checkedToken).orElseThrow(TokenNotFoundException::new);
        };
    }

    public BearerToken generateToken(User user) {
        String combined = user.getUsername()+":"+user.getEmail();
        String accessToken = createToken(combined, accessTokenValidity);
        String refreshToken = createRefreshToken();

        BearerToken bearerToken = new BearerToken();
        bearerToken.setToken(accessToken);
        bearerToken.setRefreshToken(refreshToken);
        bearerToken.setUser(user);

        BearerToken token = repo.save(bearerToken);
        logger.debug("Generated BearerToken: {}", token);

        return token;
    }

    public Optional<BearerToken> refreshToken(String refreshToken) {
        return repo.findByRefreshToken(refreshToken)
                .map(existingToken -> {
                    String newAccessToken = createToken(UUID.randomUUID().toString(), accessTokenValidity);
                    String newRefreshToken = createRefreshToken();
                    existingToken.setToken(newAccessToken);
                    existingToken.setRefreshToken(newRefreshToken);
                    return Optional.of(repo.save(existingToken));
                }).orElse(Optional.empty());
    }

    public void logoutToken(String token) {
        repo.findByToken(token).ifPresent(repo::delete);
    }

    public boolean validateTokenPair(String accessToken, String refreshToken) {
        return repo.findByToken(accessToken)
                .filter(token -> token.getRefreshToken().equals(refreshToken))
                .isPresent();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public LocalDateTime getTokenExpirationDate(String token) {
        Claims claims = extractClaims(token);
        return Instant.ofEpochMilli(claims.getExpiration().getTime())
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    private String createToken(String subject, long expiration) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken() {
        return UUID.randomUUID().toString();
    }
}
