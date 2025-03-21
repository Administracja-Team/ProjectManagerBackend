package me.gnevilkoko.project_manager.models.exceptions;

import org.springframework.http.HttpStatus;

public class FailedToOperateImageException extends BaseApiException{

    public FailedToOperateImageException(HttpStatus status, String message) {
        super(status, message);
    }
}
