package backend.authservice.service;

import backend.authservice.dto.AuthResponse;
import backend.authservice.dto.Role;
import backend.authservice.dto.UserResponse;
import backend.authservice.entity.UserEntity;
import backend.authservice.exception.UserAlreadyExistsException;
import backend.authservice.repository.UserJpaRepository;
import backend.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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

    @Transactional
    public UserResponse register(String username, String password, String email, Role role) {
        if (userJpaRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("User already exists");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(passwordEncoder.encode(password));
        userEntity.setEmail(email);
        userEntity.setRole(Objects.requireNonNullElse(role, Role.SUBSCRIBER));
        userJpaRepository.save(userEntity);

        return new UserResponse(userEntity.getId(), userEntity.getUsername(), userEntity.getEmail(), userEntity.getRole());
    }

    public AuthResponse login(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(token);
    }
}
