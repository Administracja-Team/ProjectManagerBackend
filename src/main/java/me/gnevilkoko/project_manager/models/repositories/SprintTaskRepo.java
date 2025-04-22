package me.gnevilkoko.project_manager.models.repositories;

import me.gnevilkoko.project_manager.models.entities.SprintTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SprintTaskRepo extends JpaRepository<SprintTask, Long> {

}
