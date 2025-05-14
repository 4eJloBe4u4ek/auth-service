package backend.authservice.service;

import backend.authservice.dto.*;
import backend.authservice.entity.UserEntity;
import backend.authservice.exception.InvalidTotpCodeException;
import backend.authservice.exception.UserAlreadyExistsException;
import backend.authservice.repository.UserJpaRepository;
import backend.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserJpaRepository userJpaRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final GAService gaService;

    @Transactional
    public UserResponse register(String username, String password, String email, Role role) {
        if (userJpaRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User already exists");
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(Objects.requireNonNullElse(role, Role.UNASSIGNED));
        userJpaRepository.save(user);

        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    @Transactional
    public AuthResponse login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserEntity user = userJpaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), userDetails);
        boolean roleMissing = user.getRole() == Role.UNASSIGNED;
        boolean hasSecret = user.getTotpSecret() != null;

        return new AuthResponse(token, roleMissing, !hasSecret, hasSecret);
    }

    public AuthResponse setRole(Role role, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());

        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        user.setRole(role);
        userJpaRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), userDetails);
        boolean hasSecret = user.getTotpSecret() != null;

        return new AuthResponse(token, false, !hasSecret, hasSecret);
    }

    public UserInfoResponse getUserInfo(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());

        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        return new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getPhone()
        );
    }

    @Transactional
    public UserInfoResponse updateUserInfo(UserInfoRequest req, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());

        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        if (userJpaRepository.existsByEmail(req.email()) && !user.getEmail().equals(req.email())) {
            throw new UserAlreadyExistsException("User already exists");
        }

        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPhone(req.phone());
        userJpaRepository.save(user);

        return new UserInfoResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getPhone());
    }

    public Setup2FaResponse setup2Fa(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        String secret = gaService.generateSecret();
        user.setTotpSecret(secret);
        userJpaRepository.save(user);

        String uri = gaService.buildOtpAuthUrl(user.getEmail(), secret);
        String qrCodeBase64 = gaService.generateQRBase64(uri);
        return new Setup2FaResponse(uri, qrCodeBase64);
    }

    public AuthResponse verify2Fa(int code, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        if (gaService.verifySecret(user.getTotpSecret(), code)) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), userDetails);
            return new AuthResponse(token, false, true,false);
        } else {
            throw new InvalidTotpCodeException("Invalid TOTP code");
        }
    }

    public UserInfoResponse getUserInfoById(Long userId) {
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        return new UserInfoResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getPhone());
    }
}
