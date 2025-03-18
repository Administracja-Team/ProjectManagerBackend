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
    private String username;

    @NotBlank(message = "Can't be blank")
    @Email(message = "Is not a real email address")
    private String email;

    @NotBlank(message = "Can't be blank")
    @Size(min = 3, max = 64, message = "Length must be >= 3 and <= 64")
    @Schema(
            description = "Пароль должен содержать от 3 до 64 символов, допускаются только латинские буквы и цифры",
            minLength = 3,
            maxLength = 64,
            example = "secret123"
    )
    private String password;

    @NotBlank(message = "Cant' be blank")
    @Size(min = 2, max = 2, message = "Language code length must be 2 letters")
    @JsonProperty("language_code")
    private String languageCode;

    @NotBlank(message = "Cant' be blank")
    @Size(min = 2, max = 48, message = "Is not a real name")
    private String name;

    @NotBlank(message = "Cant' be blank")
    @Size(min = 2, max = 48, message = "Is not a real surname")
    private String surname;
}
