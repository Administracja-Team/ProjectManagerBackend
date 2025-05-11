package me.gnevilkoko.project_manager.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.gnevilkoko.project_manager.models.entities.Project;
import me.gnevilkoko.project_manager.models.entities.Sprint;
import me.gnevilkoko.project_manager.models.entities.SprintTask;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SprintDTO {
    private long id;

    private String name;

    private String description;

    @JsonProperty("start_at")
    private LocalDateTime startAt;

    @JsonProperty("end_at")
    private LocalDateTime endAt;

    private List<SprintTaskDTO> tasks = new ArrayList<>();

    @JsonProperty("done_percents")
    private double donePercents = 0;

    public SprintDTO(Sprint sprint, long memberId) {
        this.id = sprint.getId();
        this.name = sprint.getName();
        this.description = sprint.getDescription();
        this.startAt = sprint.getStartAt();
        this.endAt = sprint.getEndAt();
        this.donePercents = sprint.getDonePercents();

        for (SprintTask task : sprint.getTasks()) {
            tasks.add(new SprintTaskDTO(task, memberId));
        }
    }
}
