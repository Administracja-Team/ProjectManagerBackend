package me.gnevilkoko.project_manager.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.gnevilkoko.project_manager.models.entities.ProjectMember;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LowDataProjectMemberDTO {
    private long id;

    public LowDataProjectMemberDTO(ProjectMember projectMember) {
        this.id = projectMember.getId();
    }
}
