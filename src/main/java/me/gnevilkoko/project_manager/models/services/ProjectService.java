package me.gnevilkoko.project_manager.models.services;

import jakarta.transaction.Transactional;
import me.gnevilkoko.project_manager.models.dto.ShortProjectMemberDTO;
import me.gnevilkoko.project_manager.models.dto.ProjectMemberDTO;
import me.gnevilkoko.project_manager.models.dto.requests.ProjectCreateRequest;
import me.gnevilkoko.project_manager.models.entities.Project;
import me.gnevilkoko.project_manager.models.entities.ProjectInvitationCode;
import me.gnevilkoko.project_manager.models.entities.ProjectMember;
import me.gnevilkoko.project_manager.models.entities.User;
import me.gnevilkoko.project_manager.models.exceptions.MemberNotFoundException;
import me.gnevilkoko.project_manager.models.exceptions.ProjectNotFoundException;
import me.gnevilkoko.project_manager.models.exceptions.ReceivedWrongDataException;
import me.gnevilkoko.project_manager.models.repositories.ProjectInvitationCodeRepo;
import me.gnevilkoko.project_manager.models.repositories.ProjectMemberRepo;
import me.gnevilkoko.project_manager.models.repositories.ProjectRepo;
import me.gnevilkoko.project_manager.models.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ProjectService {
    private ProjectMemberRepo memberRepo;
    private ProjectRepo projectRepo;
    private UserRepo userRepo;
    private ProjectInvitationCodeRepo codeRepo;


    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private long codeExpire;

    @Autowired
    public ProjectService(ProjectMemberRepo memberRepo, ProjectRepo projectRepo, UserRepo userRepo, ProjectInvitationCodeRepo codeRepo, @Value("${project.invitation-code.expire}") long codeExpire) {
        this.memberRepo = memberRepo;
        this.projectRepo = projectRepo;
        this.userRepo = userRepo;
        this.codeRepo = codeRepo;
        this.codeExpire = codeExpire;
    }

    public ProjectMember createProjectMember(Project project, User user, ProjectMember.SystemRole role){
        ProjectMember projectMember = new ProjectMember(project, user);
        projectMember.setSystemRole(role);
        projectMember = memberRepo.save(projectMember);

        return projectMember;
    }

    public Project createProject(ProjectCreateRequest request){
        Project project = new Project(request.getName(), request.getDescription());
        project = projectRepo.save(project);

        return project;
    }

    @Transactional
    public List<ProjectMemberDTO> getAllUserProjects(User user) {
        user = userRepo.findById(user.getId()).get();
        List<ProjectMemberDTO> memberDtos = new ArrayList<>();

        for (ProjectMember member : user.getProjects()) {
            memberDtos.add(new ProjectMemberDTO(member));
        }
        return memberDtos;
    }

    @Transactional
    public List<ShortProjectMemberDTO> getShortAllUserProjects(User user) {
        user = userRepo.findById(user.getId()).get();
        List<ShortProjectMemberDTO> memberDtos = new ArrayList<>();

        for (ProjectMember member : user.getProjects()) {
            memberDtos.add(new ShortProjectMemberDTO(member));
        }
        return memberDtos;
    }

    @Transactional
    public Optional<ProjectMemberDTO> getProjectDetails(User user, long projectId) {
        Optional<Project> optionalProject = projectRepo.findById(projectId);
        if(optionalProject.isEmpty())
            return Optional.empty();

        ProjectMember projectMember = user.getProjects().stream().filter(pr -> pr.getProject().getId() == projectId).findFirst().orElse(null);
        if(projectMember == null)
            return Optional.empty();

        return Optional.of(new ProjectMemberDTO(projectMember));
    }

    public ProjectMember getProjectMemberOrThrow(long memberId) {
        return memberRepo.findById(memberId).orElseThrow(MemberNotFoundException::new);
    }

    public Project getProjectOrThrow(long projectId) {
        return projectRepo.findById(projectId).orElseThrow(ProjectNotFoundException::new);
    }

    public ProjectMember getProjectMemberOrThrow(long projectId, long userId) {
        Project project = getProjectOrThrow(projectId);
        ProjectMember member = project.getMembers().stream().filter(m -> m.getUser().getId() == userId).findFirst().orElseThrow(MemberNotFoundException::new);
        return member;
    }


    @Transactional
    public void leaveProject(long projectId, long userId) {
        ProjectMember member = getProjectMemberOrThrow(projectId, userId);

        if (member.getSystemRole() == ProjectMember.SystemRole.OWNER) {
            throw new ReceivedWrongDataException("You can't leave from your project");
        }

        if(!isUserConnectedToProject(userId, projectId)){
            throw new ReceivedWrongDataException("You are not connected to this project");
        }

        member.getUser().getProjects().remove(member);
        memberRepo.save(member);
    }

    @Transactional
    public void deleteProject(Project project) {
        projectRepo.delete(project);
    }

    @Transactional
    public boolean isUserHasAdminPermissionInProject(long userId, long projectId) {
        Optional<Project> optionalProject = projectRepo.findById(projectId);
        if(optionalProject.isEmpty())
            return false;

        Project project = optionalProject.get();
        User user = userRepo.findById(userId).get();

        for(ProjectMember member : project.getMembers()) {
            if(member.getUser().getId() == user.getId()){
                return member.getSystemRole() != ProjectMember.SystemRole.MEMBER;
            }
        }

        return false;
    }

    public boolean isUserConnectedToProject(long userId, long projectId) {
        Optional<Project> optionalProject = projectRepo.findById(projectId);
        if(optionalProject.isEmpty())
            return false;

        Project project = optionalProject.get();
        User user = userRepo.findById(userId).get();
        for(ProjectMember member : user.getProjects()) {
            if(member.getProject().getId() == project.getId()){
                return true;
            }
        }

        return false;
    }

    public ProjectInvitationCode generateInvitationCode(Project project) {
        String code;
        do {
            code = generateRandomCode();
        } while (codeRepo.existsByCode(code));

        ProjectInvitationCode invitationCode = new ProjectInvitationCode(
                code,
                project,
                LocalDateTime.now(),
                LocalDateTime.now().plusSeconds(codeExpire)
        );

        codeRepo.save(invitationCode);
        return invitationCode;
    }

    @Transactional
    public void deleteMember(ProjectMember pm) {
        memberRepo.delete(pm);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void deleteExpiredInvitationCodes() {
        codeRepo.deleteExpiredCodes(LocalDateTime.now());
    }

    private String generateRandomCode(){
        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        int codeLength = 6;
        for (int i = 0; i < codeLength; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
