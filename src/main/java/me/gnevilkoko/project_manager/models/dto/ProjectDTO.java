package me.gnevilkoko.project_manager.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.gnevilkoko.project_manager.models.entities.Project;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDTO {
    private long id;
    private String name;
    private String description;

    public ProjectDTO(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
    }
}
