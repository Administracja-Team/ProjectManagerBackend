package me.gnevilkoko.project_manager.models.repositories;

import me.gnevilkoko.project_manager.models.entities.BearerToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BearerTokenRepo extends JpaRepository<BearerToken, UUID> {

    Optional<BearerToken> findByToken(String token);
    Optional<BearerToken> findByRefreshToken(String refreshToken);
}
