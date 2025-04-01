package me.gnevilkoko.project_manager.models.exceptions;

import org.springframework.http.HttpStatus;

public class AlreadyConnectedException extends BaseApiException{

    public AlreadyConnectedException() {
        super(HttpStatus.CONFLICT, "Already connected");
    }
}
