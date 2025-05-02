package org.novize.api.controller;

import org.novize.api.dtos.UserDto;
import org.novize.api.model.User;
import org.novize.api.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling user-related requests.
 * This controller provides endpoints for fetching user information and managing users.
 */

@RequestMapping("/api/users")
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint to get the authenticated user's information.
     * @return The authenticated user's information.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserDto authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User currentUser = (User) authentication.getPrincipal();
        // Map the User object to UserDto

        return UserDto.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .firstname(currentUser.getFirstname())
                .lastname(currentUser.getLastname())
                .xp(currentUser.getXp())
                .build();
    }


    /**
     * Endpoint to get all users.
     * @return A list of all users.
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<User>> allUsers() {
        List <User> users = userService.allUsers();

        return ResponseEntity.ok(users);
    }


}