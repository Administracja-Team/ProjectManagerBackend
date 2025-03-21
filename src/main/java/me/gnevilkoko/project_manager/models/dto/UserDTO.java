package me.gnevilkoko.project_manager.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.gnevilkoko.project_manager.models.entities.User;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDTO {
    private String username;
    private String email;

    private String name;
    private String surname;

    @JsonProperty("language_code")
    private String languageCode;

    @JsonProperty("registered_at")
    private LocalDateTime registeredAt;

    public UserDTO(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.languageCode = user.getLanguageCode();
        this.registeredAt = user.getRegisteredAt();
    }
}
