package me.gnevilkoko.project_manager.models.exceptions;

import org.springframework.http.HttpStatus;

public class ProjectNotFoundException extends BaseApiException{
    public ProjectNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "Project(ID: "+id+") was not found in database");
    }
}
