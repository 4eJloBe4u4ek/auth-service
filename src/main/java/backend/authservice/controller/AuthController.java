package backend.authservice.controller;

import backend.authservice.dto.*;
import backend.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request.username(), request.password(), request.email(), request.role()));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(request.email(), request.password()));
    }

    @PostMapping("/auth/set-role")
    public ResponseEntity<AuthResponse> setRole(@RequestBody RoleRequest roleRequest, Authentication auth) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.setRole(roleRequest.role(), auth));
    }

    @GetMapping("/auth/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(Authentication auth) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.getUserInfo(auth));
    }

    @PutMapping("/auth/me")
    public ResponseEntity<UserInfoResponse> updateProfile(
            @RequestBody UserInfoRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(authService.updateUserInfo(req, auth));
    }
}
