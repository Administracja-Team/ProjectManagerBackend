package me.gnevilkoko.project_manager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.gnevilkoko.project_manager.models.dto.ProjectMemberDTO;
import me.gnevilkoko.project_manager.models.dto.ShortProjectMemberDTO;
import me.gnevilkoko.project_manager.models.dto.ShortSprintDTO;
import me.gnevilkoko.project_manager.models.dto.SprintDTO;
import me.gnevilkoko.project_manager.models.dto.requests.CreateSprintRequest;
import me.gnevilkoko.project_manager.models.dto.requests.ProjectCreateRequest;
import me.gnevilkoko.project_manager.models.dto.requests.StringRequest;
import me.gnevilkoko.project_manager.models.entities.*;
import me.gnevilkoko.project_manager.models.exceptions.*;
import me.gnevilkoko.project_manager.models.repositories.ProjectInvitationCodeRepo;
import me.gnevilkoko.project_manager.models.repositories.ProjectMemberRepo;
import me.gnevilkoko.project_manager.models.repositories.ProjectRepo;
import me.gnevilkoko.project_manager.models.repositories.SprintRepo;
import me.gnevilkoko.project_manager.models.services.AvatarStorageService;
import me.gnevilkoko.project_manager.models.services.ProjectService;
import me.gnevilkoko.project_manager.models.services.SprintService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/project", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Project manipulations", description = "Create, get and manage user projects")
public class ProjectController {

    private final ProjectRepo projectRepo;
    private final ProjectMemberRepo memberRepo;
    private final ProjectInvitationCodeRepo codeRepo;
    private final ProjectService projectService;
    private final AvatarStorageService avatarService;
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    public ProjectController(ProjectService projectService, ProjectRepo projectRepo, ProjectInvitationCodeRepo codeRepo, ProjectMemberRepo memberRepo, AvatarStorageService avatarService) {
        this.projectService = projectService;
        this.projectRepo = projectRepo;
        this.codeRepo = codeRepo;
        this.memberRepo = memberRepo;
        this.avatarService = avatarService;
    }

    @GetMapping("/list")
    @Operation(summary = "Get all user projects",
            description = "Returns a list of all projects where the current user is assigned")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of projects returned successfully",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ShortProjectMemberDTO.class))
                    )
            )
    })
    public ResponseEntity<List<ShortProjectMemberDTO>> getAllUserProjects() {
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        logger.info("Request to get all projects for {} user", user.getId());
        List<ShortProjectMemberDTO> memberDtos = projectService.getShortAllUserProjects(user);


        logger.debug("Found {} projects for user {}", memberDtos.size(), user.getUsername());
        return ResponseEntity.ok(memberDtos);
    }

    @GetMapping("/{project_id}")
    @Operation(summary = "Get project details",
            description = "Returns information about project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of projects returned successfully",
                    content = @Content(schema = @Schema(implementation = ProjectMemberDTO.class)))
    })
    public ResponseEntity<ProjectMemberDTO> getProjectDetails(@PathVariable("project_id") long projectId) {
        logger.info("Request to get all projects for current user");
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();

        if(!projectService.isUserConnectedToProject(user.getId(), projectId)){
            throw new NotEnoughPermissionsException();
        }

        Optional<ProjectMemberDTO> optionalProjectMember = projectService.getProjectDetails(user, projectId);
        if(optionalProjectMember.isEmpty())
            throw new MemberNotFoundException();

        return ResponseEntity.ok(optionalProjectMember.get());
    }

    @Operation(
            summary = "Get member avatar",
            description = "Returns the avatar image (PNG) for the specified project member if you have access"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar image returned successfully",
                    content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE)),
            @ApiResponse(responseCode = "404", description = "Member not found",
                    content = @Content(schema = @Schema(implementation = MemberNotFoundException.class))),
            @ApiResponse(responseCode = "403", description = "User is not connected to this project",
                    content = @Content(schema = @Schema(implementation = NotEnoughPermissionsException.class))),
            @ApiResponse(responseCode = "500", description = "Failed to get avatar image",
                    content = @Content(schema = @Schema(implementation = FailedToOperateImageException.class)))
    })
    @GetMapping(path="/member/{member_id}/avatar", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getMemberAvatar(@PathVariable("member_id") long memberId)  {
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();

        ProjectMember requestingMember = projectService.getProjectMemberOrThrow(memberId);
        ProjectMember member = projectService.getProjectMemberOrThrow(requestingMember.getProject().getId(), user.getId());

        try {
            return ResponseEntity.ok(avatarService.getAvatar(requestingMember.getUser()));
        } catch (IOException e) {
            throw new FailedToOperateImageException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get avatar image");
        }
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
    public ResponseEntity<ShortProjectMemberDTO> connectToProject(@PathVariable(name = "code") String code) {
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

        ProjectMember projectMember = projectService.createProjectMember(project, user, ProjectMember.SystemRole.MEMBER);
        project.addMember(projectMember);
        projectRepo.save(project);
        logger.info("User {} connected to project id: {} using invitation code {}", user.getUsername(), project.getId(), code);
        return ResponseEntity.ok(new ShortProjectMemberDTO(projectMember));
    }

    @PostMapping("/create")
    @Operation(summary = "Create new empty project and assign user as owner of this project",
            description = "Creates a new project and assigns the currently authenticated user as the owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project was created successfully",
                    content = @Content(schema = @Schema(implementation = ProjectMemberDTO.class))),
    })
    public ResponseEntity<ProjectMemberDTO> createProject(@org.springframework.web.bind.annotation.RequestBody ProjectCreateRequest request) {
        logger.info("Received request to create project with name: {}", request.getName());
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();

        Project project = projectService.createProject(request);
        logger.debug("Project created with id: {}", project.getId());

        ProjectMember projectMember = projectService.createProjectMember(project, user, ProjectMember.SystemRole.OWNER);
        project.addMember(projectMember);
        projectRepo.save(project);
        logger.info("User {} assigned as owner to project id: {}", user.getUsername(), project.getId());

        ProjectMemberDTO test = new ProjectMemberDTO(projectMember);
        return ResponseEntity.ok(test);
    }

    @PostMapping("/member/{member_id}/descriptive-role")
    @Operation(summary = "Update user descriptive role in project where you admin/owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member role set successfully"),
            @ApiResponse(responseCode = "404", description = "Member is not found",
                    content = @Content(schema = @Schema(implementation = MemberNotFoundException.class))),
            @ApiResponse(responseCode = "403", description = "User is not admin or owner",
                    content = @Content(schema = @Schema(implementation = NotEnoughPermissionsException.class)))
    })
    public ResponseEntity<Void> setRoleToMember(@PathVariable("member_id") long memberId, @Valid @org.springframework.web.bind.annotation.RequestBody StringRequest request){
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        ProjectMember member = projectService.getProjectMemberOrThrow(memberId);
        Project project = member.getProject();

        if(!projectService.isUserHasAdminPermissionInProject(user.getId(), project.getId())){
            throw new NotEnoughPermissionsException();
        }

        member.setDescriptiveRole(request.getPayload());
        memberRepo.save(member);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/member/{member_id}/system-role")
    @Operation(summary = "Update user system role in project where you admin/owner", description = "Can be set only to MEMBER or ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member role set successfully"),
            @ApiResponse(responseCode = "404", description = "Member is not found",
                    content = @Content(schema = @Schema(implementation = MemberNotFoundException.class))),
            @ApiResponse(responseCode = "403", description = "User is not admin or owner",
                    content = @Content(schema = @Schema(implementation = NotEnoughPermissionsException.class))),
            @ApiResponse(responseCode = "400", description = "Received something wrong", content = @Content(schema = @Schema(implementation = ReceivedWrongDataException.class)))
    })
    public ResponseEntity<Void> setSystemRoleToMember(@PathVariable("member_id") long memberId, @Valid @org.springframework.web.bind.annotation.RequestBody StringRequest request){
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        ProjectMember member = projectService.getProjectMemberOrThrow(memberId);

        Project project = member.getProject();
        if(!projectService.isUserHasAdminPermissionInProject(user.getId(), project.getId())){
            throw new NotEnoughPermissionsException();
        }

        if(member.getUser().getId() == user.getId()){
            throw new ReceivedWrongDataException("You can't change your system role");
        }

        if(member.getSystemRole() == ProjectMember.SystemRole.OWNER){
            throw new NotEnoughPermissionsException();
        }

        ProjectMember.SystemRole systemRole;
        try {
            systemRole = ProjectMember.SystemRole.valueOf(request.getPayload().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ReceivedWrongDataException("System role can't be \""+request.getPayload()+"\"");
        }

        if(systemRole == ProjectMember.SystemRole.OWNER){
            throw new ReceivedWrongDataException("System role can't be OWNER for this user");
        }
        member.setSystemRole(systemRole);
        memberRepo.save(member);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/member/{member_id}/delete")
    @Operation(summary = "Delete member from project if user is admin/owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member was deleted"),
            @ApiResponse(responseCode = "404", description = "Member is not found",
                    content = @Content(schema = @Schema(implementation = MemberNotFoundException.class))),
            @ApiResponse(responseCode = "403", description = "User is not admin or owner",
                    content = @Content(schema = @Schema(implementation = NotEnoughPermissionsException.class))),
            @ApiResponse(responseCode = "400", description = "Received something wrong (User is deleting himself)", content = @Content(schema = @Schema(implementation = ReceivedWrongDataException.class)))
    })
    public ResponseEntity<Void> deleteUserFromProject(@PathVariable("member_id") long memberId){
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        ProjectMember member = projectService.getProjectMemberOrThrow(memberId);
        Project project = member.getProject();

        if(!projectService.isUserHasAdminPermissionInProject(user.getId(), project.getId())){
            throw new NotEnoughPermissionsException();
        }

        if(member.getUser().getId() == user.getId()){
            throw new ReceivedWrongDataException("You can't delete yourself from project");
        }

        if(member.getSystemRole() == ProjectMember.SystemRole.OWNER){
            throw new NotEnoughPermissionsException();
        }

        projectService.removeProjectMember(memberId);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Leave project",
            description = "Removes the current user from the specified project if they are not the owner"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User left project successfully"),
            @ApiResponse(responseCode = "404", description = "Project membership not found",
                    content = @Content(schema = @Schema(implementation = MemberNotFoundException.class))),
            @ApiResponse(responseCode = "403", description = "User is not connected to this project",
                    content = @Content(schema = @Schema(implementation = NotEnoughPermissionsException.class))),
            @ApiResponse(responseCode = "400", description = "Owner cannot leave their own project",
                    content = @Content(schema = @Schema(implementation = ReceivedWrongDataException.class)))
    })
    @DeleteMapping("/leave/{project_id}")
    public ResponseEntity<Void> leaveFromProject(@PathVariable("project_id") long projectId){
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        ProjectMember member = projectService.getProjectMemberOrThrow(projectId, user.getId());

        if(member.getSystemRole() == ProjectMember.SystemRole.OWNER){
            throw new ReceivedWrongDataException("You can't leave from your project");
        }

        projectService.removeProjectMember(member);

        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Delete own project",
            description = "Deletes the specified project along with all its members and data if the current user is the owner"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(schema = @Schema(implementation = ProjectNotFoundException.class))),
            @ApiResponse(responseCode = "403", description = "User is not the project owner",
                    content = @Content(schema = @Schema(implementation = NotEnoughPermissionsException.class)))
    })
    @DeleteMapping("/delete/{project_id}")
    public ResponseEntity<Void> deleteOwnProject(@PathVariable("project_id") long projectId){
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        ProjectMember projectMember = projectService.getProjectMemberOrThrow(projectId, user.getId());
        Project project = projectService.getProjectOrThrow(projectId);

        if(projectMember.getSystemRole() != ProjectMember.SystemRole.OWNER){
            throw new NotEnoughPermissionsException();
        }

        projectService.deleteProject(project.getId());
        return ResponseEntity.noContent().build();
    }
}
