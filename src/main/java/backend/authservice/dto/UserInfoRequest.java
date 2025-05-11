package backend.authservice.dto;

public record UserInfoRequest(
        String username,
        String email,
        String phone
) {
}
