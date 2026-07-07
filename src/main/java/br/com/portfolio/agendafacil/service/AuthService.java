package br.com.portfolio.agendafacil.service;

import br.com.portfolio.agendafacil.domain.User;
import br.com.portfolio.agendafacil.dto.AuthDtos.AuthResponse;
import br.com.portfolio.agendafacil.dto.AuthDtos.LoginRequest;
import br.com.portfolio.agendafacil.dto.AuthDtos.RegisterRequest;
import br.com.portfolio.agendafacil.exception.BusinessException;
import br.com.portfolio.agendafacil.repository.UserRepository;
import br.com.portfolio.agendafacil.security.AuthenticatedUser;
import br.com.portfolio.agendafacil.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("E-mail ja cadastrado");
        }

        var user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        userRepository.save(user);

        return buildResponse(new AuthenticatedUser(user));
    }

    public AuthResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        return buildResponse((AuthenticatedUser) authentication.getPrincipal());
    }

    private AuthResponse buildResponse(AuthenticatedUser user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                user.id(),
                user.name(),
                user.email(),
                user.role()
        );
    }
}
