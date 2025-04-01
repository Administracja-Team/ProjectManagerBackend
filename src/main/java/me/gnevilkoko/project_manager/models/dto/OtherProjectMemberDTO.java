package me.gnevilkoko.project_manager.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.gnevilkoko.project_manager.models.entities.ProjectMember;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtherProjectMemberDTO {
    private long id;
    private UserDTO user;

    @JsonProperty("system_role")
    private ProjectMember.SystemRole systemRole;

    @JsonProperty("descriptive_role")
    private String descriptiveRole;

    public OtherProjectMemberDTO(ProjectMember projectMember) {
        this.id = projectMember.getId();
        this.user = new UserDTO(projectMember.getUser());
        this.systemRole = projectMember.getSystemRole();
        this.descriptiveRole = projectMember.getDescriptiveRole();
    }
}
