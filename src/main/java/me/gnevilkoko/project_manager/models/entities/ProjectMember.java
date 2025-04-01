package me.gnevilkoko.project_manager.models.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project_members")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "system_role")
    @JsonProperty("system_role")
    private SystemRole systemRole = SystemRole.MEMBER;

    @Column(name = "descriptive_role")
    @JsonProperty("descriptive_role")
    private String descriptiveRole = "";

    public ProjectMember(Project project, User user) {
        this.project = project;
        this.user = user;
    }


    public enum SystemRole {
        OWNER,
        ADMIN,
        MEMBER
    }
}
