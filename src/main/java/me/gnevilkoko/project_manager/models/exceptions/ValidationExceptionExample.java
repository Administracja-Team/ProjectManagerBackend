package me.gnevilkoko.project_manager.models.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ValidationExceptionExample {
    private String name = "Can't be blank";
    private String email = "Is not a real email address";

    @JsonProperty("any_other_filed")
    private String anyOtherField = "Any other exception message";
}
