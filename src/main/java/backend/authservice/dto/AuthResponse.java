package backend.authservice.dto;

public record AuthResponse(String token, boolean roleMissing, boolean need2faSetup, boolean need2faVerify) { }
