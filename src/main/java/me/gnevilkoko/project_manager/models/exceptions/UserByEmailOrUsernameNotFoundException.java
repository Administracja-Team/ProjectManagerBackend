package me.gnevilkoko.project_manager.models.exceptions;

import org.springframework.http.HttpStatus;

public class UserByEmailOrUsernameNotFoundException extends BaseApiException{

    public UserByEmailOrUsernameNotFoundException() {
        super(HttpStatus.NOT_FOUND, "User not found by email or username");
    }
}
