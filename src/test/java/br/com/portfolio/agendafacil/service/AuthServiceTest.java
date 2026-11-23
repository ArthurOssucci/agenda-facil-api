package br.com.portfolio.agendafacil.service;

import br.com.portfolio.agendafacil.domain.User;
import br.com.portfolio.agendafacil.domain.UserRole;
import br.com.portfolio.agendafacil.dto.AuthDtos.RegisterRequest;
import br.com.portfolio.agendafacil.repository.UserRepository;
import br.com.portfolio.agendafacil.security.AuthenticatedUser;
import br.com.portfolio.agendafacil.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthService authService = new AuthService(
            userRepository, passwordEncoder, mock(AuthenticationManager.class), jwtService);

    @Test
    void anonymousCallerCannotRegisterAdmin() {
        var request = new RegisterRequest("Eve", "eve@email.com", "123456", UserRole.ADMIN);

        assertThatThrownBy(() -> authService.register(request, null))
                .isInstanceOf(AccessDeniedException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void clientCallerCannotRegisterProfessional() {
        var request = new RegisterRequest("Eve", "eve@email.com", "123456", UserRole.PROFESSIONAL);

        assertThatThrownBy(() -> authService.register(request, callerWithRole(UserRole.CLIENT)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void missingRoleDefaultsToClient() {
        var request = new RegisterRequest("Ana", "ana@email.com", "123456", null);
        when(passwordEncoder.encode("123456")).thenReturn("hash");

        var response = authService.register(request, null);

        assertThat(response.role()).isEqualTo(UserRole.CLIENT);
    }

    @Test
    void adminCallerCanRegisterProfessional() {
        var request = new RegisterRequest("Dr. Silva", "silva@email.com", "123456", UserRole.PROFESSIONAL);
        when(passwordEncoder.encode("123456")).thenReturn("hash");

        var response = authService.register(request, callerWithRole(UserRole.ADMIN));

        assertThat(response.role()).isEqualTo(UserRole.PROFESSIONAL);
    }

    private AuthenticatedUser callerWithRole(UserRole role) {
        var user = new User();
        user.setRole(role);
        return new AuthenticatedUser(user);
    }
}
