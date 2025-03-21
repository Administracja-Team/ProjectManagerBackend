package me.gnevilkoko.project_manager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.gnevilkoko.project_manager.models.dto.UserDTO;
import me.gnevilkoko.project_manager.models.dto.requests.UserUpdateProfileRequest;
import me.gnevilkoko.project_manager.models.entities.BearerToken;
import me.gnevilkoko.project_manager.models.entities.User;
import me.gnevilkoko.project_manager.models.exceptions.FailedToOperateImageException;
import me.gnevilkoko.project_manager.models.exceptions.WrongCredentialsException;
import me.gnevilkoko.project_manager.models.repositories.UserRepo;
import me.gnevilkoko.project_manager.models.services.AvatarStorageService;
import me.gnevilkoko.project_manager.models.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    @Autowired
    public UserController(AvatarStorageService avatarService, UserService userService, UserRepo userRepo) {
        this.avatarService = avatarService;
        this.userService = userService;
        this.userRepo = userRepo;
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
            )
    })
    public ResponseEntity<Void> updateUserProfile(@RequestBody UserUpdateProfileRequest request){
        BearerToken token = (BearerToken) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = token.getUser();

        userService.updateUserData(request, user);
        userRepo.save(user);

        return ResponseEntity.noContent().build();
    }
}
