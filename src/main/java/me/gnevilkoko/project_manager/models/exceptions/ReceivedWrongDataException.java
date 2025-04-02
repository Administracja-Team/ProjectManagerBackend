package me.gnevilkoko.project_manager.models.exceptions;

import org.springframework.http.HttpStatus;

public class ReceivedWrongDataException extends BaseApiException{

    public ReceivedWrongDataException(String badDataInformation) {
        super(HttpStatus.BAD_REQUEST, badDataInformation);
    }
}
