package me.gnevilkoko.project_manager.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.gnevilkoko.project_manager.models.entities.ProjectMember;
import me.gnevilkoko.project_manager.models.entities.Sprint;
import me.gnevilkoko.project_manager.models.entities.SprintTask;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SprintTaskDTO {
    private long id;

    private String name;

    private String description;

    @JsonProperty("start_at")
    private LocalDateTime startAt;

    @JsonProperty("end_at")
    private LocalDateTime endAt;

    private SprintTask.Priority priority;

    private List<LowDataProjectMemberDTO> implementers = new ArrayList<>();

    public SprintTaskDTO(SprintTask sprintTask) {
        this.id = sprintTask.getId();
        this.name = sprintTask.getName();
        this.description = sprintTask.getDescription();
        this.startAt = sprintTask.getStartAt();
        this.endAt = sprintTask.getEndAt();
        this.priority = sprintTask.getPriority();

        for (ProjectMember projectMember : sprintTask.getImplementers()) {
            implementers.add(new LowDataProjectMemberDTO(projectMember));
        }
    }
}
