package me.gnevilkoko.project_manager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.gnevilkoko.project_manager.models.dto.requests.UserLoginRequest;
import me.gnevilkoko.project_manager.models.dto.requests.UserTokensRequest;
import me.gnevilkoko.project_manager.models.dto.requests.UserRegistrationRequest;
import me.gnevilkoko.project_manager.models.dto.BearerTokenDTO;
import me.gnevilkoko.project_manager.models.entities.BearerToken;
import me.gnevilkoko.project_manager.models.entities.User;
import me.gnevilkoko.project_manager.models.exceptions.*;
import me.gnevilkoko.project_manager.models.repositories.UserRepo;
import me.gnevilkoko.project_manager.models.services.AvatarStorageService;
import me.gnevilkoko.project_manager.models.services.BearerTokenService;
import me.gnevilkoko.project_manager.models.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.AbstractMap;
import java.util.Optional;

@RestController
@RequestMapping(
        value = "/authorization",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@SecurityRequirement(name = "")
@Tag(name = "Authorization", description = "Methods for authorize users (login/registration)")
public class AuthorizationController {
    private AvatarStorageService avatarService;
    private BearerTokenService bearerTokenService;
    private UserService userService;
    private UserRepo userRepo;
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);


    @Autowired
    public AuthorizationController(BearerTokenService bearerTokenService,
                                   UserService userService, UserRepo userRepo,
                                   PasswordEncoder passwordEncoder,
                                   AvatarStorageService avatarService) {
        this.bearerTokenService = bearerTokenService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
        this.avatarService = avatarService;
    }


    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
                                    examples = {
                                            @ExampleObject(
                                                    value = "{\"code\": 0, \"message\": \"User field \\\"joe@example.com\\\" already exist\"}"
                                            ),
                                            @ExampleObject(
                                                    value = "{\"code\": 1, \"message\": \"User field \\\"joe_doe\\\" already exist\"}"
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "417",
                            description = "Received wrong image (only .png/.jpg accepted)",
                            content = @Content(
                                    schema = @Schema(implementation = WrongCredentialsException.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = "{\"message\": \"some additional information\"}"
                                            )
                                    }
                            )
                    )
            }
    )
    public ResponseEntity<BearerTokenDTO> registerUser(
            @Parameter(
                    name = "user",
                    description = "Registration user data",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserRegistrationRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"username\":\"john_doe\",\"email\":\"john@example.com\",\"password\":\"123456\",\"name\":\"Joe\",\"surname\":\"Doe\",\"language_code\":\"en\"}"
                            )
                    )
            )
            @Valid @RequestPart("user") UserRegistrationRequest request,
            @Parameter(
                    name = "avatar",
                    description = "User avatar file",
                    content = @Content(
                            mediaType = MediaType.IMAGE_PNG_VALUE
                    )
            )
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile
    ) {
        logger.debug("Registration attempt with data: {}", request);


        Optional<AbstractMap.SimpleEntry<Integer, String>> optionalSearchedUserData =
                userService.checkUserExists(request.getUsername(), request.getEmail());
        if (optionalSearchedUserData.isPresent()){
            AbstractMap.SimpleEntry<Integer, String> searchedUserData = optionalSearchedUserData.get();
            logger.debug("Registration fail for {}, because email is exists", request.getUsername());

            throw new RegisteringUserDataAlreadyExistException(
                    new AbstractMap.SimpleEntry<>(searchedUserData.getKey(), searchedUserData.getValue()));
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


        try {
            boolean imageStatus;
            if (avatarFile != null && !avatarFile.isEmpty()){
                imageStatus = avatarService.uploadCustomAvatar(user, avatarFile);
            } else {
                imageStatus = avatarService.generateAvatar(user);
            }

            if (!imageStatus) {
                logger.warn("Avatar upload failed for user: {}", user.getUsername());
                throw new FailedToOperateImageException(HttpStatus.EXPECTATION_FAILED, "Wrong avatar file");
            }
        } catch (IOException e) {
            throw new FailedToOperateImageException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save image");
        }


        BearerToken token = bearerTokenService.generateToken(user);
        BearerTokenDTO tokenDTO = new BearerTokenDTO(token, bearerTokenService);
        return ResponseEntity.ok(tokenDTO);
    }



    @PostMapping("/login")
    @Operation(summary = "Log in and get token by email or username as identifier")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User was successfully logged in"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found (not email, not username)",
                            content = @Content(
                                    schema = @Schema(implementation = UserByEmailOrUsernameNotFoundException.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = "{\"message\": \"some additional information\"}"
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User exist, but received wrong credentials",
                            content = @Content(
                                    schema = @Schema(implementation = WrongCredentialsException.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = "{\"message\": \"some additional information\"}"
                                            )
                                    }
                            )
                    )
            }
    )

    public ResponseEntity<BearerTokenDTO> loginUser(@Valid @org.springframework.web.bind.annotation.RequestBody UserLoginRequest request){
        logger.debug("Authorization attempt with data: {}", request);

        Optional<User> searchedUser = userRepo.findByUsernameOrEmail(request.getIdentifier(), request.getIdentifier());
        if(searchedUser.isEmpty()){
            logger.debug("User was not found");
            throw new UserByEmailOrUsernameNotFoundException();
        }
        User user = searchedUser.get();

        if(!passwordEncoder.matches(request.getPassword(), user.getHash())){
            logger.debug("Wrong credentials for login");
            throw new WrongCredentialsException();
        }

        BearerToken token = bearerTokenService.generateToken(user);
        BearerTokenDTO tokenDTO = new BearerTokenDTO(token, bearerTokenService);

        logger.info("Successfully logged in {}", user);
        return ResponseEntity.ok(tokenDTO);
    }


    @DeleteMapping("/logout")
    @Operation(summary = "Log out and delete user token")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Token was successfully deleted"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Received wrong credentials",
                            content = @Content(
                                    schema = @Schema(implementation = WrongCredentialsException.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = "{\"message\": \"some additional information\"}"
                                            )
                                    }
                            )
                    )
            }
    )
    @RequestBody(
            description = "Token data for log out",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserTokensRequest.class),
                    examples = @ExampleObject(
                            value = "{\"access_token\": \"abcd.abcd123.aced\", \"refresh_token\": \"aaaaa-bbbbb-ccccc\"}"
                    )
            )
    )
    public ResponseEntity<Void> logoutUser(@Valid @org.springframework.web.bind.annotation.RequestBody UserTokensRequest request){
        if(!bearerTokenService.validateTokenPair(request.getToken(), request.getRefreshToken())){
            logger.debug("Wrong credentials for logout {}", request);
            throw new WrongCredentialsException();
        }

        bearerTokenService.logoutToken(request.getToken());
        logger.info("Successfully logged out");

        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/refresh")
    @Operation(summary = "Refreshes already existing tokens")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Token was successfully refreshed"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Received wrong credentials",
                            content = @Content(
                                    schema = @Schema(implementation = WrongCredentialsException.class),
                                    examples = {
                                            @ExampleObject(
                                                    value = "{\"message\": \"some additional information\"}"
                                            )
                                    }
                            )
                    )
            }
    )
    @RequestBody(
            description = "Token data for refreshing",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserTokensRequest.class),
                    examples = @ExampleObject(
                            value = "{\"access_token\": \"abcd.abcd123.aced\", \"refresh_token\": \"aaaaa-bbbbb-ccccc\"}"
                    )
            )
    )
    public ResponseEntity<BearerTokenDTO> refreshUserToken(@Valid @org.springframework.web.bind.annotation.RequestBody UserTokensRequest request){
        if(!bearerTokenService.validateTokenPair(request.getToken(), request.getRefreshToken())){
            logger.debug("Wrong credentials for refreshing {}", request);
            throw new WrongCredentialsException();
        }
        Optional<BearerToken> optionalToken = bearerTokenService.refreshToken(request.getRefreshToken());
        if(optionalToken.isEmpty()){
            logger.debug("Token is not valid {}", request);
            throw new TokenIsNotValid();
        }

        BearerToken token = optionalToken.get();
        BearerTokenDTO tokenDTO = new BearerTokenDTO(token, bearerTokenService);
        logger.info("Successfully refreshed token for {}", token.getUser());

        return ResponseEntity.ok(tokenDTO);
    }






}
