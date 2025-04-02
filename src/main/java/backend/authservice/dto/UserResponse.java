package backend.authservice.dto;

public record UserResponse(Long id, String username, String email, Role role) {
}
