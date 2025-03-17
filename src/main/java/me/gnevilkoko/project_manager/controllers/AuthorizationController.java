package me.gnevilkoko.project_manager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.gnevilkoko.project_manager.models.dto.requests.UserRegistrationRequest;
import me.gnevilkoko.project_manager.models.dto.responses.BearerTokenDTO;
import me.gnevilkoko.project_manager.models.entities.BearerToken;
import me.gnevilkoko.project_manager.models.exceptions.ValidationExceptionExample;
import me.gnevilkoko.project_manager.models.services.BearerTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        value = "/authorization",
        produces = "application/json",
        consumes = "application/json"
)
@SecurityRequirement(name = "")
@Tag(name = "Authorization", description = "Methods for authorize users (login/registration)")
public class AuthorizationController {
    private BearerTokenService bearerTokenService;

    @Autowired
    public AuthorizationController(BearerTokenService bearerTokenService) {
        this.bearerTokenService = bearerTokenService;
    }

    @PostMapping
    @Operation(summary = "Registration of new user")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User was successfully registered"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation errors occurred",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ValidationExceptionExample.class)
                            )
                    )
            }
    )
    @RequestBody(
            description = "Registration user data",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserRegistrationRequest.class),
                    examples = @ExampleObject(
                            value = "{\"username\": \"john_doe\", \"email\": \"john@example.com\", \"password\": \"secret123\"}"
                    )
            )
    )
    public ResponseEntity<BearerTokenDTO> registerUser(@Valid @org.springframework.web.bind.annotation.RequestBody UserRegistrationRequest request){

        return ResponseEntity.ok(null);
    }
}
