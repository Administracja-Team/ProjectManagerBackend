package me.gnevilkoko.project_manager.models.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "project", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProjectMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "project",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<Sprint> sprints = new ArrayList<>();

    @JsonProperty("created_at")
    private LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC);

    public Project(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void addMember(ProjectMember projectMember) {
        members.add(projectMember);
    }

    public void removeMember(ProjectMember pm) {
        members.remove(pm);
        pm.setProject(null);
    }

    public double getDonePercents(){
        long allSprints = getSprints().size();
        long doneSprints = getSprints().stream().filter(Sprint::isEnded).count();

        return (double) (100 * doneSprints) / allSprints;
    }
}
