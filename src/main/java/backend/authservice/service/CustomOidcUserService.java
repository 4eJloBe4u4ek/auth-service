package backend.authservice.service;

import backend.authservice.dto.Role;
import backend.authservice.entity.UserEntity;
import backend.authservice.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {
    private final UserJpaRepository userRepo;

    @Override
    public OidcUser loadUser(OidcUserRequest req) {
        OidcUser oidc = super.loadUser(req);
        String email = oidc.getEmail();
        userRepo.findByEmail(email)
                .orElseGet(() -> {
                    var u = new UserEntity();
                    u.setUsername(oidc.getFullName());
                    u.setEmail(email);
                    u.setPassword("");
                    u.setRole(Role.UNASSIGNED);
                    return userRepo.save(u);
                });
        return oidc;
    }
}

