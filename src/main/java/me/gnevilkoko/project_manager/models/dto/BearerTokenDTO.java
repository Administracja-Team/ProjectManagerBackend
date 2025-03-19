package me.gnevilkoko.project_manager.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.gnevilkoko.project_manager.models.entities.BearerToken;
import me.gnevilkoko.project_manager.models.services.BearerTokenService;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BearerTokenDTO {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;

    public BearerTokenDTO(BearerToken token, BearerTokenService service) {
        this.accessToken = token.getToken();
        this.refreshToken = token.getRefreshToken();
        this.expiresAt = service.getTokenExpirationDate(accessToken);
    }
}
