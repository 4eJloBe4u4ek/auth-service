package backend.authservice.service;

import backend.authservice.dto.AuthResponse;
import backend.authservice.dto.Role;
import backend.authservice.entity.UserEntity;
import backend.authservice.repository.UserJpaRepository;
import backend.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.*;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.*;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final UserJpaRepository userRepo;
    private final UserDetailsService userDetailsService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication auth) throws IOException {
        OidcUser oidc = (OidcUser) auth.getPrincipal();
        UserEntity user = userRepo.findByEmail(oidc.getEmail()).get();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), userDetails);
        boolean missing = user.getRole() == Role.UNASSIGNED;

        String frontendBase = "http://localhost:3000";
        String redirectUrl = String.format("%s/auth?token=%s&roleMissing=%b",
                frontendBase, token, missing);
        res.sendRedirect(redirectUrl);
    }
}
