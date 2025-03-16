package me.gnevilkoko.project_manager.models.exceptions;

import org.springframework.http.HttpStatus;

public class TokenNotFoundException extends BaseApiException {

    public TokenNotFoundException() {
        super(HttpStatus.FORBIDDEN, "Bearer token doesn't exist");
    }
}
