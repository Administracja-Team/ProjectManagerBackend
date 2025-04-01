package me.gnevilkoko.project_manager.models.exceptions;

import org.springframework.http.HttpStatus;

public class NotEnoughPermissionsException extends BaseApiException{
    public NotEnoughPermissionsException() {
        super(HttpStatus.FORBIDDEN, "You don't have enough permissions to perform this operation");
    }
}
