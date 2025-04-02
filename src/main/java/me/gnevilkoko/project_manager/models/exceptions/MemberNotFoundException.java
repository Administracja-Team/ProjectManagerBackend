package me.gnevilkoko.project_manager.models.exceptions;

import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends BaseApiException{

    public MemberNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Member is not found");
    }
}
