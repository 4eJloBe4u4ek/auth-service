package backend.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "encryption")
public record EncryptionProperties (String key) {
}
