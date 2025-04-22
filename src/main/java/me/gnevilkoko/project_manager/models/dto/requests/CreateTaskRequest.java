package me.gnevilkoko.project_manager.models.dto.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.gnevilkoko.project_manager.models.entities.SprintTask;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateTaskRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    @JsonProperty("start_at")
    private LocalDateTime startAt;

    @NotNull
    @JsonProperty("end_at")
    private LocalDateTime endAt;

    @NotNull
    private SprintTask.Priority priority;

    @NotEmpty
    @JsonProperty("implementer_member_ids")
    private List<Long> implementerMemberIds;
}
