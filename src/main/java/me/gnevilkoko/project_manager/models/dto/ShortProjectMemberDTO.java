package me.gnevilkoko.project_manager.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.gnevilkoko.project_manager.models.entities.ProjectMember;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ShortProjectMemberDTO {

    private ProjectDTO project;

    @JsonProperty("system_role")
    private ProjectMember.SystemRole systemRole;

    @JsonProperty("owner_name")
    private String ownerName;

    public ShortProjectMemberDTO(ProjectMember member) {
        this.project = new ProjectDTO(member.getProject());
        this.systemRole = member.getSystemRole();

        ProjectMember owner = member.getProject().getMembers().stream().filter(m -> m.getSystemRole() == ProjectMember.SystemRole.OWNER).findFirst().orElse(null);
        this.ownerName = owner.getUser().getUsername();
    }
}
