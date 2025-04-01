package me.gnevilkoko.project_manager.models.exceptions;

import org.springframework.http.HttpStatus;

public class WrongInvitationCodeException extends BaseApiException{

    public WrongInvitationCodeException() {
        super(HttpStatus.FORBIDDEN, "Received wrong invitation code");
    }
}
