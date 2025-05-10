package me.gnevilkoko.project_manager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.gnevilkoko.project_manager.models.dto.ShortSprintDTO;
import me.gnevilkoko.project_manager.models.dto.SprintDTO;
import me.gnevilkoko.project_manager.models.dto.requests.CreateSprintRequest;
import me.gnevilkoko.project_manager.models.dto.requests.StringRequest;
import me.gnevilkoko.project_manager.models.entities.*;
import me.gnevilkoko.project_manager.models.exceptions.NotEnoughPermissionsException;
import me.gnevilkoko.project_manager.models.exceptions.ReceivedWrongDataException;
import me.gnevilkoko.project_manager.models.repositories.SprintRepo;
import me.gnevilkoko.project_manager.models.services.ProjectService;
import me.gnevilkoko.project_manager.models.services.SprintService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController()
@RequestMapping(value = "/project", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Sprints for projects", description = "Manage project sprints")
public class SprintController {
    private final SprintService sprintService;
    private final SprintRepo sprintRepo;
    private ProjectService projectService;

    @Autowired
    public SprintController(SprintService sprintService, SprintRepo sprintRepo, ProjectService projectService) {
        this.sprintService = sprintService;
        this.sprintRepo = sprintRepo;
        this.projectService = projectService;
    }

    @Operation(
            summary = "Get all sprints for project",
            description = "Returns a list of all sprints for the specified project if the current user is a member"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of sprints returned successfully",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ShortSprintDTO.class))
                    )
            ),
            @ApiResponse(responseCode = "403", description = "User is not a member of this project",
                    content = @Content(schema = @Schema(implementation = NotEnoughPermissionsException.class)))
    })
    @GetMapping("/{project_id}/sprints")
    public ResponseEntity<List<ShortSprintDTO>> getShortSprintData(@PathVariable("project_id") long projectId) {
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();

        if (!projectService.isUserConnectedToProject(user.getId(), projectId)) {
            throw new NotEnoughPermissionsException();
        }

        List<ShortSprintDTO> sprints = sprintService.getAllSprints(projectId);
        return ResponseEntity.ok(sprints);
    }

    @Operation(
            summary = "Get sprint details",
            description = "Returns full details of a specific sprint, including its tasks, if the user is connected to the project"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sprint details returned successfully",
                    content = @Content(schema = @Schema(implementation = SprintDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "User is not a member of this project",
                    content = @Content(schema = @Schema(implementation = NotEnoughPermissionsException.class))),
            @ApiResponse(responseCode = "404", description = "Sprint not found",
                    content = @Content(schema = @Schema(implementation = me.gnevilkoko.project_manager.models.exceptions.SprintNotFoundException.class)))
    })
    @GetMapping("/{project_id}/sprint/{sprint_id}")
    public ResponseEntity<SprintDTO> getSprintDetails(@PathVariable("project_id") long projectId, @PathVariable("sprint_id") long sprintId) {
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        if (!projectService.isUserConnectedToProject(user.getId(), projectId)) {
            throw new NotEnoughPermissionsException();
        }

        Sprint sprint = sprintService.getSprint(sprintId);
        return ResponseEntity.ok(new SprintDTO(sprint));
    }

    @Operation(
            summary = "Create new sprint",
            description = "Creates a sprint along with its tasks in the specified project; requires admin or owner role"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sprint created successfully",
                    content = @Content(schema = @Schema(implementation = SprintDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "User does not have permission to create sprint",
                    content = @Content(schema = @Schema(implementation = NotEnoughPermissionsException.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(schema = @Schema(implementation = me.gnevilkoko.project_manager.models.exceptions.ProjectNotFoundException.class)))
    })
    @PostMapping("/{project_id}/sprint")
    public ResponseEntity<SprintDTO> createSprint(@Valid @org.springframework.web.bind.annotation.RequestBody CreateSprintRequest request,
                                                  @PathVariable("project_id") Long projectId) {
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        if (!projectService.isUserHasAdminPermissionInProject(user.getId(), projectId)) {
            throw new NotEnoughPermissionsException();
        }

        Sprint sprint = sprintService.createSprint(projectId, request);
        return ResponseEntity.ok(new SprintDTO(sprint));
    }

    @Operation(
            summary = "Delete sprint",
            description = "Deletes the specified sprint and all its tasks if the current user has admin or owner role"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sprint deleted successfully"),
            @ApiResponse(responseCode = "403", description = "User does not have permission to delete sprint",
                    content = @Content(schema = @Schema(implementation = NotEnoughPermissionsException.class))),
            @ApiResponse(responseCode = "404", description = "Sprint not found",
                    content = @Content(schema = @Schema(implementation = me.gnevilkoko.project_manager.models.exceptions.SprintNotFoundException.class)))
    })
    @DeleteMapping("/{project_id}/sprint/{sprint_id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable("project_id") long projectId, @PathVariable("sprint_id") long sprintId) {
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        ProjectMember member = projectService.getProjectMemberOrThrow(projectId);
        Project project = member.getProject();

        if (!projectService.isUserHasAdminPermissionInProject(user.getId(), project.getId())) {
            throw new NotEnoughPermissionsException();
        }

        Sprint sprint = sprintService.getSprint(sprintId);
        sprintRepo.delete(sprint);
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Change sprint task status",
            description = "You should be owner/admin or implementer of this task to perform change.\n" +
                    "Available types: TODO, IN_PROGRESS, DONE"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Status was changed"),
            @ApiResponse(responseCode = "403", description = "Not enough permissions"),
            @ApiResponse(responseCode = "404", description = "Task/Sprint/Project not found")
    })
    @PatchMapping("/{project_id}/sprint/{sprint_id}/{task_id}")
    public ResponseEntity<Void> changeTaskStatus(@PathVariable("project_id") long projectId,
                                                 @PathVariable("sprint_id") long sprintId,
                                                 @PathVariable("task_id") long taskId,
                                                 @Valid @org.springframework.web.bind.annotation.RequestBody StringRequest request)
    {
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        ProjectMember member = projectService.getProjectMemberOrThrow(projectId, user.getId());
        Project project = member.getProject();
        Sprint sprint = sprintService.getSprint(sprintId);
        SprintTask sprintTask = sprintService.getSprintTask(sprintId, taskId);
        ProjectMember projectMember = projectService.getProjectMemberOrThrow(projectId, user.getId());

        if(!projectService.isUserHasAdminPermissionInProject(user.getId(), projectId) && !sprintTask.getImplementers().contains(projectMember)) {
            throw new NotEnoughPermissionsException();
        }

        sprintService.updateSprintTaskStatus(sprintTask, request.getPayload());
        return ResponseEntity.noContent().build();
    }
}
