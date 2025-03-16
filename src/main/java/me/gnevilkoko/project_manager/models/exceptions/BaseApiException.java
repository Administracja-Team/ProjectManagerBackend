package me.gnevilkoko.project_manager.models.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"stackTrace", "cause", "suppressed", "localizedMessage"})
public class BaseApiException extends RuntimeException {
    @JsonIgnore
    private HttpStatus status;

    @JsonProperty("code")
    private Integer customCode;

    private String message;

    public BaseApiException(HttpStatus status, int customCode) {
        this.status = status;
        this.customCode = customCode;
    }

    public BaseApiException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public String toJson(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Fail to parse object to json";
        }
    }
}
