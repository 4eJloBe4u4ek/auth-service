package backend.authservice.dto;

public record Setup2FaResponse(String secret, String qrCodeBase64) {
}
