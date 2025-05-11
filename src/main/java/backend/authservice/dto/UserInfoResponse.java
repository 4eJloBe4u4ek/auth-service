package backend.authservice.dto;

public record UserInfoResponse(
        String username,
        String email,
        Role role,
        String phone
) {}
