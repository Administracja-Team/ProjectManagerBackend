package me.gnevilkoko.project_manager.models.services;

import me.gnevilkoko.project_manager.models.dto.requests.UserUpdateProfileRequest;
import me.gnevilkoko.project_manager.models.entities.User;
import me.gnevilkoko.project_manager.models.repositories.UserRepo;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;
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

    public void updateUserData(UserUpdateProfileRequest source, User target){
        BeanWrapper src = new BeanWrapperImpl(source);
        BeanWrapper trg = new BeanWrapperImpl(target);

        for (PropertyDescriptor pd : src.getPropertyDescriptors()) {
            String propertyName = pd.getName();
            Object srcValue = src.getPropertyValue(propertyName);
            if (srcValue != null && trg.isWritableProperty(propertyName)) {
                trg.setPropertyValue(propertyName, srcValue);
            }
        }
    }
}
