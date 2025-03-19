package me.gnevilkoko.project_manager.models.dto.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserTokensRequest {

    @NotBlank(message = "Can't be blank")
    @JsonProperty("access_token")
    private String token;

    @NotBlank(message = "Can't be blank")
    @JsonProperty("refresh_token")
    private String refreshToken;
}
