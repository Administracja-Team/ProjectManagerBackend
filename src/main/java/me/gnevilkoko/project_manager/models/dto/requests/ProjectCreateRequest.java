package me.gnevilkoko.project_manager.models.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreateRequest {
    @Schema(example = "Discord Bot")
    private String name;

    @Schema(example = "Example goal of the project")
    private String description;
}
