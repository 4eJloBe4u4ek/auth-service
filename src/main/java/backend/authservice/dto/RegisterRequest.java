package backend.authservice.dto;

public record RegisterRequest(String username, String password, String email, Role role) {
}
