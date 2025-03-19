package me.gnevilkoko.project_manager.models.exceptions;


import org.springframework.http.HttpStatus;

public class TokenIsNotValid extends BaseApiException {

    public TokenIsNotValid() {
        super(HttpStatus.FORBIDDEN, "Token is not valid");
    }
}
