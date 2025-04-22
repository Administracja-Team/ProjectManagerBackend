package me.gnevilkoko.project_manager.models.exceptions;

import org.springframework.http.HttpStatus;

public class SprintNotFoundException extends BaseApiException{

    public SprintNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Sprint was not founded");
    }
}
