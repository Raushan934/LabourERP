package in.gov.labourerp.controller;

import in.gov.labourerp.dto.auth.UserDto;
import in.gov.labourerp.dto.common.ApiResponse;
import in.gov.labourerp.dto.user.ChangePasswordRequest;
import in.gov.labourerp.dto.user.CreateUserRequest;
import in.gov.labourerp.dto.user.ResetPasswordRequest;
import in.gov.labourerp.dto.user.UpdateUserRequest;
import in.gov.labourerp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {
        String createdBy = authentication != null ? authentication.getName() : "ADMIN";
        UserDto created = userService.createUser(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User created and synchronized with Supabase Auth", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        String updatedBy = authentication != null ? authentication.getName() : "ADMIN";
        UserDto updated = userService.updateUser(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("User deputation updated successfully", updated));
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request,
            Authentication authentication) {
        String adminUsername = authentication != null ? authentication.getName() : "ADMIN";
        userService.resetPassword(id, request.getNewPassword(), adminUsername);
        return ResponseEntity.ok(ApiResponse.success("Password reset and synchronized with Supabase Auth successfully", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Not authenticated"));
        }
        userService.changePassword(authentication.getName(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password changed and synchronized with Supabase Auth successfully", null));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Not authenticated"));
        }
        String fullName = body.get("fullName");
        String phone = body.get("phone");
        UserDto updated = userService.updateProfile(authentication.getName(), fullName, phone);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }
}
