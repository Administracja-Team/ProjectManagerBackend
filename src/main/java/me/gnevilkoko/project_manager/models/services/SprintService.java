package me.gnevilkoko.project_manager.models.services;

import jakarta.transaction.Transactional;
import me.gnevilkoko.project_manager.models.dto.ShortSprintDTO;
import me.gnevilkoko.project_manager.models.dto.requests.CreateSprintRequest;
import me.gnevilkoko.project_manager.models.dto.requests.CreateTaskRequest;
import me.gnevilkoko.project_manager.models.entities.Project;
import me.gnevilkoko.project_manager.models.entities.ProjectMember;
import me.gnevilkoko.project_manager.models.entities.Sprint;
import me.gnevilkoko.project_manager.models.entities.SprintTask;
import me.gnevilkoko.project_manager.models.exceptions.SprintNotFoundException;
import me.gnevilkoko.project_manager.models.repositories.SprintRepo;
import me.gnevilkoko.project_manager.models.repositories.SprintTaskRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SprintService {
    private final ProjectService projectService;
    private final SprintRepo sprintRepo;
    private final SprintTaskRepo sprintTaskRepo;

    @Autowired
    public SprintService(ProjectService projectService, SprintRepo sprintRepo, SprintTaskRepo sprintTaskRepo) {
        this.projectService = projectService;
        this.sprintRepo = sprintRepo;
        this.sprintTaskRepo = sprintTaskRepo;
    }

    @Transactional
    public Sprint createSprint(long projectId, CreateSprintRequest request) {
        Project project = projectService.getProjectOrThrow(projectId);

        Sprint sprint = new Sprint();
        sprint.setName(request.getName());
        sprint.setDescription(request.getDescription());
        sprint.setStartAt(request.getStartAt());
        sprint.setEndAt(request.getEndAt());
        sprint.setProject(project);
        sprint = sprintRepo.save(sprint);

        for (CreateTaskRequest tr : request.getTasks()) {
            SprintTask task = new SprintTask();
            task.setName(tr.getName());
            task.setPriority(tr.getPriority());
            task.setSprint(sprint);

            for (Long memberId : tr.getImplementerMemberIds()) {
                ProjectMember pm = projectService.getProjectMemberOrThrow(memberId);
                task.getImplementers().add(pm);
            }

            sprintTaskRepo.save(task);
            sprint.getTasks().add(task);
        }

        return sprintRepo.save(sprint);
    }

    public List<ShortSprintDTO> getAllSprints(long projectId) {
        Project project = projectService.getProjectOrThrow(projectId);
        List<ShortSprintDTO> sprints = new ArrayList<>();

        for(Sprint sprint : project.getSprints()) {
            sprints.add(sprint.toShortSprintDTO());
        }

        return sprints;
    }

    public Sprint getSprint(long sprintId) {
        return sprintRepo.findById(sprintId).orElseThrow(SprintNotFoundException::new);
    }
}
