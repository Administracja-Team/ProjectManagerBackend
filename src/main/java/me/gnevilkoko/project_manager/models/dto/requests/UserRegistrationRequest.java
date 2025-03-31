package me.gnevilkoko.project_manager.models.dto.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserRegistrationRequest {

    @NotBlank(message = "Can't be blank")
    @Schema(example = "john_doe")
    private String username;

    @NotBlank(message = "Can't be blank")
    @Email(message = "Is not a real email address")
    @Schema(example = "john@example.com")
    private String email;

    @NotBlank(message = "Can't be blank")
    @Size(min = 3, max = 64, message = "Length must be >= 3 and <= 64")
    @Schema(
            minLength = 3,
            maxLength = 64,
            example = "123456"
    )
    private String password;

    @NotBlank(message = "Cant' be blank")
    @Size(min = 2, max = 2, message = "Language code length must be 2 letters")
    @JsonProperty("language_code")
    @Schema(example = "en")
    private String languageCode;

    @NotBlank(message = "Cant' be blank")
    @Size(min = 2, max = 48, message = "Is not a real name")
    @Schema(example = "Joe")
    private String name;

    @NotBlank(message = "Cant' be blank")
    @Size(min = 2, max = 48, message = "Is not a real surname")
    @Schema(example = "Doe")
    private String surname;
}
