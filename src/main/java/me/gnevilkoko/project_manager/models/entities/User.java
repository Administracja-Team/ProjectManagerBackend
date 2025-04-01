package me.gnevilkoko.project_manager.models.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.gnevilkoko.project_manager.models.dto.requests.UserRegistrationRequest;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {

    @Id
    @GeneratedValue
    private long id;

    private String username;
    private String email;
    private String hash;
    private String name;
    private String surname;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JsonProperty("language_code")
    private String languageCode;

    @JsonProperty("registered_at")
    private LocalDateTime registeredAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectMember> projects;

    public User(String username, String email, String hash, String name, String surname, String languageCode, LocalDateTime registeredAt) {
        this.username = username;
        this.email = email;
        this.hash = hash;
        this.name = name;
        this.surname = surname;
        this.languageCode = languageCode;
        this.registeredAt = registeredAt;
    }
}
