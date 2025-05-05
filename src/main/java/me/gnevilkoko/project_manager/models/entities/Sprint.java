package me.gnevilkoko.project_manager.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.gnevilkoko.project_manager.models.dto.ShortSprintDTO;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sprints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JsonProperty("start_at")
    @Column(name = "start_at")
    private LocalDateTime startAt;

    @JsonProperty("end_at")
    @Column(name = "end_at")
    private LocalDateTime endAt;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JsonIgnore
    private Project project;

    @OneToMany(mappedBy = "sprint",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true)
    private List<SprintTask> tasks = new ArrayList<>();


    public ShortSprintDTO toShortSprintDTO() {
        ShortSprintDTO shortSprintDTO = new ShortSprintDTO();
        shortSprintDTO.setId(id);
        shortSprintDTO.setName(name);
        shortSprintDTO.setDescription(description);
        shortSprintDTO.setStartTime(startAt);
        shortSprintDTO.setEndTime(endAt);
        shortSprintDTO.setTasks(tasks.size());

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        shortSprintDTO.setEnded(isEnded());
        shortSprintDTO.setStarted(isStarted());

        return shortSprintDTO;
    }

    public boolean isEnded(){
        return LocalDateTime.now(ZoneOffset.UTC).isAfter(endAt);
    }

    public boolean isStarted(){
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return now.isAfter(startAt) && now.isBefore(endAt) || now.equals(startAt);
    }
}
