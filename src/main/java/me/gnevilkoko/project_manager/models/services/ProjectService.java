package me.gnevilkoko.project_manager.models.services;

import jakarta.transaction.Transactional;
import me.gnevilkoko.project_manager.models.dto.ProjectMemberDTO;
import me.gnevilkoko.project_manager.models.dto.requests.ProjectCreateRequest;
import me.gnevilkoko.project_manager.models.entities.Project;
import me.gnevilkoko.project_manager.models.entities.ProjectInvitationCode;
import me.gnevilkoko.project_manager.models.entities.ProjectMember;
import me.gnevilkoko.project_manager.models.entities.User;
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

    public ProjectMember generateProjectMember(Project project, User user, ProjectMember.SystemRole role){
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

        User user = userRepo.findById(userId).get();
        for(ProjectMember member : user.getProjects()) {
            if(member.getUser().getId() == user.getId()){
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
