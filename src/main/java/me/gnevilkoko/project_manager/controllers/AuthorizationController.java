package me.gnevilkoko.project_manager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.gnevilkoko.project_manager.models.dto.requests.UserRegistrationRequest;
import me.gnevilkoko.project_manager.models.dto.responses.BearerTokenDTO;
import me.gnevilkoko.project_manager.models.entities.BearerToken;
import me.gnevilkoko.project_manager.models.entities.User;
import me.gnevilkoko.project_manager.models.exceptions.RegisteringUserDataAlreadyExistException;
import me.gnevilkoko.project_manager.models.exceptions.ValidationExceptionExample;
import me.gnevilkoko.project_manager.models.repositories.UserRepo;
import me.gnevilkoko.project_manager.models.services.BearerTokenService;
import me.gnevilkoko.project_manager.models.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.AbstractMap;
import java.util.Optional;

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
    private UserService userService;
    private UserRepo userRepo;
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);


    @Autowired
    public AuthorizationController(BearerTokenService bearerTokenService, UserService userService, UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.bearerTokenService = bearerTokenService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
    }

    @PostMapping("/register")
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
                                    schema = @Schema(implementation = ValidationExceptionExample.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Email or username already exist",
                            content = @Content(
                                    schema = @Schema(implementation = RegisteringUserDataAlreadyExistException.class),
                                    examples = @ExampleObject(
                                            value = "{\"code\": 0, \"message\": \"User field \\\"joe@example.com\\\" already exist\"}"
                                    )
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
                            value = "{\"username\": \"john_doe\", \"email\": \"john@example.com\", \"password\": \"secret123\"," +
                                    "\"name\": \"Joe\", \"surname\": \"Doe\", \"language_code\": \"en\"}"
                    )
            )
    )
    public ResponseEntity<BearerTokenDTO> registerUser(@Valid @org.springframework.web.bind.annotation.RequestBody UserRegistrationRequest request){
        logger.debug("Registration attempt with data: {}", request);

        Optional<AbstractMap.SimpleEntry<Integer, String>> optionalSearchedUserData = userService.checkUserExists(request.getUsername(), request.getEmail());
        if(optionalSearchedUserData.isPresent()){
            AbstractMap.SimpleEntry<Integer, String> searchedUserData = optionalSearchedUserData.get();
            logger.debug("Registration fail for {}, because email is exists", request.getUsername());
            throw new RegisteringUserDataAlreadyExistException(new AbstractMap.SimpleEntry<>(searchedUserData.getKey(), searchedUserData.getValue()));
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordHash,
                request.getName(),
                request.getSurname(),
                request.getLanguageCode(),
                LocalDateTime.now(ZoneOffset.UTC)
        );
        userRepo.save(user);

        logger.info("Successfully registered {}", user);
        return ResponseEntity.ok(null);
    }
}
