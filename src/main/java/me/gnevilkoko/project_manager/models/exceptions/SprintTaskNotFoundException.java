package me.gnevilkoko.project_manager.models.exceptions;

import org.springframework.http.HttpStatus;

public class SprintTaskNotFoundException extends BaseApiException{
    public SprintTaskNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Sprint task not found");
    }
}
