package me.gnevilkoko.project_manager.models.exceptions;

import org.springframework.http.HttpStatus;

import java.util.AbstractMap;
import java.util.Map;

public class RegisteringUserDataAlreadyExistException extends BaseApiException{
    public RegisteringUserDataAlreadyExistException(AbstractMap.SimpleEntry<Integer, String> dataType) {
        super(HttpStatus.CONFLICT, dataType.getKey(), "User field \""+dataType.getValue()+"\" already exist");
    }
}
