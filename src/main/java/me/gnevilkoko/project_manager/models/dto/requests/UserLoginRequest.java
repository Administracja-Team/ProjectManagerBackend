package me.gnevilkoko.project_manager.models.dto.requests;

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
public class UserLoginRequest {

    @NotBlank(message = "Can't be blank")
    @Size(min = 2, max = 48, message = "Wrong length of field")
    private String identifier;

    @NotBlank(message = "Can't be blank")
    private String password;
}
