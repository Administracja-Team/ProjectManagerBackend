package me.gnevilkoko.project_manager.models.exceptions;

import org.springframework.http.HttpStatus;

public class FailedToSaveImageException extends BaseApiException{

    public FailedToSaveImageException(HttpStatus status, String message) {
        super(status, message);
    }
}
