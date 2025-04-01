package me.gnevilkoko.project_manager.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_invitation_codes")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectInvitationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    private String code;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    private Project project;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;

    public ProjectInvitationCode(String code, Project project, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.code = code;
        this.project = project;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
}
