package me.gnevilkoko.project_manager.configs;

import me.gnevilkoko.project_manager.models.exceptions.BaseApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionConfig {

    @ExceptionHandler(BaseApiException.class)
    public ResponseEntity<BaseApiException> handleBaseApiException(BaseApiException e) {
        System.out.println("PEPEPE");
        return ResponseEntity.status(e.getStatus()).body(e);
    }
}
