package me.gnevilkoko.project_manager.models.dto.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserUpdateProfileRequest {

    @Schema(example = "Joe")
    private String name;

    @Schema(example = "Doe")
    private String surname;

    @Schema(example = "john_doe")
    private String username;

    @Schema(example = "john@example.com")
    @Email
    private String email;

    @JsonProperty("language_code")
    @Schema(example = "pl")
    private String languageCode;

    @Schema(example = "Any information about person")
    private String description;
}
