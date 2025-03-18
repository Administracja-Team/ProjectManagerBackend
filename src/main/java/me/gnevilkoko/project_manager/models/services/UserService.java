package me.gnevilkoko.project_manager.models.services;

import me.gnevilkoko.project_manager.models.entities.User;
import me.gnevilkoko.project_manager.models.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Optional;

@Service
public class UserService {
    private UserRepo repo;

    @Autowired
    public UserService(UserRepo repo) {
        this.repo = repo;
    }

    public Optional<AbstractMap.SimpleEntry<Integer, String>> checkUserExists(String username, String email) {
        Optional<User> searchedUser = repo.findByUsernameOrEmail(username, email);
        return searchedUser.map(user ->
                user.getEmail().equals(email)
                        ? new AbstractMap.SimpleEntry<>(0, email)
                        : new AbstractMap.SimpleEntry<>(1, username)
        );
    }
}
