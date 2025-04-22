package me.gnevilkoko.project_manager.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.gnevilkoko.project_manager.models.entities.Project;
import me.gnevilkoko.project_manager.models.entities.ProjectMember;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberDTO {

    @JsonProperty("member_id")
    @JsonIgnore
    private long id;

    private ProjectDTO project;

    private List<OtherProjectMemberDTO> others;

    @JsonProperty("system_role")
    private ProjectMember.SystemRole systemRole;

    @JsonProperty("descriptive_role")
    private String descriptiveRole;

    public ProjectMemberDTO(ProjectMember member) {
        Project project = member.getProject();

        this.id = member.getId();
        this.project = new ProjectDTO(project);
        this.systemRole = member.getSystemRole();
        this.descriptiveRole = member.getDescriptiveRole();

        others = new ArrayList<>();
        for(ProjectMember otherMember : project.getMembers()) {

            others.add(new OtherProjectMemberDTO(otherMember));
        }
    }
}
