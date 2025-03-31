package me.gnevilkoko.project_manager.models.dto.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserPasswordUpdateRequest{

    @NotBlank
    @JsonProperty("old_password")
    @Size(min = 3, max = 64, message = "Length must be >= 3 and <= 64")
    private String oldPassword;

    @NotBlank
    @Size(min = 3, max = 64, message = "Length must be >= 3 and <= 64")
    @JsonProperty("new_password")
    private String newPassword;
}
