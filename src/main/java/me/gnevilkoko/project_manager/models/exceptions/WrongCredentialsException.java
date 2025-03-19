package me.gnevilkoko.project_manager.models.exceptions;

import org.springframework.http.HttpStatus;

public class WrongCredentialsException extends BaseApiException{

    public WrongCredentialsException() {
        super(HttpStatus.FORBIDDEN, "Wrong credentials");
    }
}
