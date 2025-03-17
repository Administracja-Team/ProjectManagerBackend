package me.gnevilkoko.project_manager.models.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import me.gnevilkoko.project_manager.models.entities.BearerToken;
import me.gnevilkoko.project_manager.models.exceptions.TokenNotFoundException;
import me.gnevilkoko.project_manager.models.repositories.BearerTokenRepo;
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

    public BearerToken generateToken() {
        String accessToken = createToken(UUID.randomUUID().toString(), accessTokenValidity);
        String refreshToken = createToken(UUID.randomUUID().toString(), refreshTokenValidity);

        BearerToken bearerToken = new BearerToken();
        bearerToken.setToken(accessToken);
        bearerToken.setRefreshToken(refreshToken);

        return repo.save(bearerToken);
    }

    public Optional<BearerToken> refreshToken(String refreshToken) {
        return repo.findByRefreshToken(refreshToken)
                .map(existingToken -> {
                    if (!isTokenValid(refreshToken)) {
                        repo.delete(existingToken);
                        return Optional.<BearerToken>empty();
                    }
                    String newAccessToken = createToken(UUID.randomUUID().toString(), accessTokenValidity);
                    existingToken.setToken(newAccessToken);
                    return Optional.of(repo.save(existingToken));
                }).orElse(Optional.empty());
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
}
