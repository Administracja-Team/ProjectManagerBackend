package me.gnevilkoko.project_manager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.gnevilkoko.project_manager.models.dto.ProjectMemberDTO;
import me.gnevilkoko.project_manager.models.dto.requests.ProjectCreateRequest;
import me.gnevilkoko.project_manager.models.entities.BearerToken;
import me.gnevilkoko.project_manager.models.entities.Project;
import me.gnevilkoko.project_manager.models.entities.ProjectInvitationCode;
import me.gnevilkoko.project_manager.models.entities.ProjectMember;
import me.gnevilkoko.project_manager.models.entities.User;
import me.gnevilkoko.project_manager.models.exceptions.AlreadyConnectedException;
import me.gnevilkoko.project_manager.models.exceptions.NotEnoughPermissionsException;
import me.gnevilkoko.project_manager.models.exceptions.ProjectNotFoundException;
import me.gnevilkoko.project_manager.models.exceptions.WrongInvitationCodeException;
import me.gnevilkoko.project_manager.models.repositories.ProjectInvitationCodeRepo;
import me.gnevilkoko.project_manager.models.repositories.ProjectRepo;
import me.gnevilkoko.project_manager.models.services.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/project", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Project manipulations", description = "Create, get and manage user projects")
public class ProjectController {

    private final ProjectRepo projectRepo;
    private final ProjectInvitationCodeRepo codeRepo;
    private final ProjectService projectService;
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    public ProjectController(ProjectService projectService, ProjectRepo projectRepo, ProjectInvitationCodeRepo codeRepo) {
        this.projectService = projectService;
        this.projectRepo = projectRepo;
        this.codeRepo = codeRepo;
    }

    @PostMapping("/create")
    @Operation(summary = "Create new empty project and assign user as owner of this project",
            description = "Creates a new project and assigns the currently authenticated user as the owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project was created successfully",
                    content = @Content(schema = @Schema(implementation = ProjectMemberDTO.class))),
    })
    public ResponseEntity<ProjectMemberDTO> createProject(
            @RequestBody(description = "Project creation data", required = true,
                    content = @Content(schema = @Schema(implementation = ProjectCreateRequest.class)))
            ProjectCreateRequest request) {
        logger.info("Received request to create project with name: {}", request.getName());
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();

        Project project = projectService.createProject(request);
        logger.debug("Project created with id: {}", project.getId());

        ProjectMember projectMember = projectService.generateProjectMember(project, user, ProjectMember.SystemRole.OWNER);
        project.addMember(projectMember);
        projectRepo.save(project);
        logger.info("User {} assigned as owner to project id: {}", user.getUsername(), project.getId());

        return ResponseEntity.ok(new ProjectMemberDTO(projectMember));
    }

    @PostMapping("/{project_id}/code/create")
    @Operation(summary = "Create invitation code for project",
            description = "Generates and returns a new unique invitation code for the specified project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation code was created successfully",
                    content = @Content(schema = @Schema(implementation = ProjectInvitationCode.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ProjectNotFoundException.class))),
            @ApiResponse(responseCode = "403", description = "User does not have permission to create invitation code",
                    content = @Content(schema = @Schema(implementation = NotEnoughPermissionsException.class)))
    })
    public ResponseEntity<ProjectInvitationCode> createInvitationCode(@PathVariable(name = "project_id") Long projectId) {
        logger.info("Request to create invitation code for project id: {}", projectId);
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        if (!projectService.isUserHasAdminPermissionInProject(user.getId(), project.getId())) {
            logger.warn("User {} does not have admin permissions in project id: {}", user.getUsername(), project.getId());
            throw new NotEnoughPermissionsException();
        }

        ProjectInvitationCode invitationCode = projectService.generateInvitationCode(project);
        logger.info("Invitation code {} generated for project id: {}", invitationCode.getCode(), project.getId());
        return ResponseEntity.ok(invitationCode);
    }

    @PostMapping("/connect/{code}")
    @Operation(summary = "Connect to project via invitation code",
            description = "Connects the current user to the project using a valid invitation code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User connected to project successfully",
                    content = @Content(schema = @Schema(implementation = ProjectMemberDTO.class))),
            @ApiResponse(responseCode = "404", description = "Invitation code not found",
                    content = @Content(schema = @Schema(implementation = WrongInvitationCodeException.class))),
            @ApiResponse(responseCode = "409", description = "User is already connected to the project",
                    content = @Content(schema = @Schema(implementation = AlreadyConnectedException.class)))
    })
    public ResponseEntity<ProjectMemberDTO> connectToProject(@PathVariable(name = "code") String code) {
        logger.info("User requests to connect to project with invitation code: {}", code);
        Optional<ProjectInvitationCode> optionalInvitationCode = codeRepo.findByCode(code);
        if (optionalInvitationCode.isEmpty()) {
            logger.warn("Invitation code {} not found", code);
            throw new WrongInvitationCodeException();
        }

        ProjectInvitationCode invitationCode = optionalInvitationCode.get();
        if (invitationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.warn("Invitation code {} is expired", code);
            throw new WrongInvitationCodeException();
        }

        Project project = invitationCode.getProject();
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        if (projectService.isUserConnectedToProject(user.getId(), project.getId())) {
            logger.warn("User {} is already connected to project id: {}", user.getUsername(), project.getId());
            throw new AlreadyConnectedException();
        }

        ProjectMember projectMember = projectService.generateProjectMember(project, user, ProjectMember.SystemRole.MEMBER);
        project.addMember(projectMember);
        projectRepo.save(project);
        logger.info("User {} connected to project id: {} using invitation code {}", user.getUsername(), project.getId(), code);
        return ResponseEntity.ok(new ProjectMemberDTO(projectMember));
    }

    @GetMapping("/list")
    @Operation(summary = "Get all user projects",
            description = "Returns a list of all projects where the current user is assigned")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of projects returned successfully",
                    content = @Content(schema = @Schema(implementation = ProjectMemberDTO.class)))
    })
    public ResponseEntity<List<ProjectMemberDTO>> getAllUserProjects() {
        logger.info("Request to get all projects for current user");
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        List<ProjectMemberDTO> memberDtos = projectService.getAllUserProjects(user);
        logger.debug("Found {} projects for user {}", memberDtos.size(), user.getUsername());
        return ResponseEntity.ok(memberDtos);
    }
}
