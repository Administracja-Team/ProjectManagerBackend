package me.gnevilkoko.project_manager.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sprint_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SprintTask {

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

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JsonIgnore
    private Sprint sprint;

    @ManyToMany
    @JoinTable(
            name = "sprint_task_implementers",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ProjectMember> implementers = new ArrayList<>();

    public enum Priority {
        LOW, MEDIUM, HIGH
    }
}
