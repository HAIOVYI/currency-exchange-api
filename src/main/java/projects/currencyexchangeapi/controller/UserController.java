package projects.currencyexchangeapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import projects.currencyexchangeapi.service.UserService;

@RestController
@RequiredArgsConstructor
@Tag(name = "User management", description = "Endpoints for managing users")
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Block a user",
            description = "Block a user by their id. This endpoint requires ADMIN role.")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{userId}/block")
    public ResponseEntity<String> blockUser(@PathVariable("userId") Long userId) {
        userService.blockUser(userId);
        return ResponseEntity.ok("User blocked successfully");
    }
}
