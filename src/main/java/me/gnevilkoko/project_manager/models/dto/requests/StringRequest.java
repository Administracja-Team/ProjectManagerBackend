package me.gnevilkoko.project_manager.models.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StringRequest {

    @NotBlank(message = "Can't be blank")
    private String payload;
}
