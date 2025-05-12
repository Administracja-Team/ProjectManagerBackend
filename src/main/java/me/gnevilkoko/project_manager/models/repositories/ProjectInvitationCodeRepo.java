package me.gnevilkoko.project_manager.models.repositories;

import jakarta.transaction.Transactional;
import me.gnevilkoko.project_manager.models.entities.ProjectInvitationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ProjectInvitationCodeRepo extends JpaRepository<ProjectInvitationCode, Long> {
    boolean existsByCode(String code);

    @Modifying
    @Transactional
    @Query("delete from ProjectInvitationCode pic where pic.expiresAt < :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);
    Optional<ProjectInvitationCode> findByCode(String code);

    void deleteAllByProjectId(Long projectId);
}
