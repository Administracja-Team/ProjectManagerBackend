package me.gnevilkoko.project_manager.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.gnevilkoko.project_manager.models.entities.Project;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDTO {
    private long id;
    private String name;
    private String description;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("done_percents")
    private double donePercents = 0.0;

    public ProjectDTO(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.createdAt = project.getCreatedAt();


        this.donePercents = project.getSprints().isEmpty() ? 0.0 : project.getDonePercents();
    }
}
