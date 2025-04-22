package me.gnevilkoko.project_manager.models.dto.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateSprintRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    @JsonProperty("start_at")
    private LocalDateTime startAt;

    @NotNull
    @JsonProperty("end_at")
    private LocalDateTime endAt;

    @NotEmpty
    private List<CreateTaskRequest> tasks;
}
