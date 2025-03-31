package me.gnevilkoko.project_manager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.gnevilkoko.project_manager.models.dto.UserDTO;
import me.gnevilkoko.project_manager.models.dto.requests.UserLoginRequest;
import me.gnevilkoko.project_manager.models.dto.requests.UserPasswordUpdateRequest;
import me.gnevilkoko.project_manager.models.dto.requests.UserUpdateProfileRequest;
import me.gnevilkoko.project_manager.models.entities.BearerToken;
import me.gnevilkoko.project_manager.models.entities.User;
import me.gnevilkoko.project_manager.models.exceptions.FailedToOperateImageException;
import me.gnevilkoko.project_manager.models.exceptions.RegisteringUserDataAlreadyExistException;
import me.gnevilkoko.project_manager.models.exceptions.UserByEmailOrUsernameNotFoundException;
import me.gnevilkoko.project_manager.models.exceptions.WrongCredentialsException;
import me.gnevilkoko.project_manager.models.repositories.UserRepo;
import me.gnevilkoko.project_manager.models.services.AvatarStorageService;
import me.gnevilkoko.project_manager.models.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Optional;

@RestController
@RequestMapping(
        value = "/user",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "User Data", description = "Get and manipulate user information")
public class UserController {
    private AvatarStorageService avatarService;
    private UserService userService;
    private UserRepo userRepo;
    private Logger logger;
    private PasswordEncoder encoder;

    @Autowired
    public UserController(AvatarStorageService avatarService, UserService userService, UserRepo userRepo, PasswordEncoder encoder) {
        this.avatarService = avatarService;
        this.userService = userService;
        this.userRepo = userRepo;
        this.encoder = encoder;
        logger = LoggerFactory.getLogger(UserController.class);
    }

    @GetMapping()
    @Operation(summary = "Returns user profile data")
    public ResponseEntity<UserDTO> getUserProfile(){
        BearerToken token = (BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDTO dto = new UserDTO(token.getUser());

        return ResponseEntity.ok(dto);
    }

    @GetMapping(path = "/avatar", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Returns user avatar image")
    public ResponseEntity<byte[]> getUserAvatar(){
        BearerToken token = (BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            return ResponseEntity.ok(avatarService.getAvatar(token.getUser()));
        } catch (IOException e) {
            throw new FailedToOperateImageException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get avatar image");
        }
    }

    @PostMapping(path = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a custom avatar image for the user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Image was updated"
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
    })
    public ResponseEntity<Void> uploadUserAvatar(@RequestParam("file") MultipartFile file) {
        BearerToken token = (BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            boolean uploaded = avatarService.uploadCustomAvatar(token.getUser(), file);
            if (!uploaded) {
                throw new FailedToOperateImageException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload custom avatar");
            }

            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            throw new FailedToOperateImageException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload avatar image");
        }
    }

    @PostMapping()
    @Operation(
            summary = "Update user profile information",
            description = "Send only the fields that need to be updated. Any field not provided (or sent as null) will remain unchanged."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Profile was updated"
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
    })
    public ResponseEntity<Void> updateUserProfile(@org.springframework.web.bind.annotation.RequestBody UserUpdateProfileRequest request){
        BearerToken token = (BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = token.getUser();

        Optional<AbstractMap.SimpleEntry<Integer, String>> existUser = userService.checkUserExists(request.getUsername(), "");
        if (existUser.isPresent()){
            AbstractMap.SimpleEntry<Integer, String> searchedUserData = existUser.get();
            logger.debug("Update fail for {}, because email or username is exists", request.getUsername());

            throw new RegisteringUserDataAlreadyExistException(
                    new AbstractMap.SimpleEntry<>(searchedUserData.getKey(), searchedUserData.getValue()));
        }

        userService.updateUserData(request, user);
        userRepo.save(user);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password")
    @Operation(summary = "Change user password")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Password was successfully changed"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Received wrong password",
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
            description = "Password update data",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserPasswordUpdateRequest.class),
                    examples = @ExampleObject(
                            value = "{\"old_password\": \"123456\", \"new_password\": \"654321\"}"
                    )
            )
    )
    public ResponseEntity<Void> updateUserPassword(@org.springframework.web.bind.annotation.RequestBody UserPasswordUpdateRequest request){
        User user = ((BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        if(!encoder.matches(request.getOldPassword(), user.getHash())){
            logger.debug("Wrong credentials");
            throw new WrongCredentialsException();
        }

        String hash = encoder.encode(request.getNewPassword());
        user.setHash(hash);
        userRepo.save(user);

        return ResponseEntity.noContent().build();
    }
}
