package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import com.openclassrooms.starterjwt.payload.response.MessageResponse;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock JwtUtils jwtUtils;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserRepository userRepository;

    @InjectMocks AuthService authService;

    @Test
    void register_shouldSaveUser_whenEmailNotTaken() {
        SignupRequest req = new SignupRequest();
        req.setEmail("a@a.com");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setPassword("password");

        when(userRepository.existsByEmail("a@a.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("ENC");

        MessageResponse resp = authService.register(req);

        assertNotNull(resp);
        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("a@a.com")
                        && u.getFirstName().equals("John")
                        && u.getLastName().equals("Doe")
                        && u.getPassword().equals("ENC")
                        && !u.isAdmin()
        ));
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyUsed() {
        SignupRequest req = new SignupRequest();
        req.setEmail("a@a.com");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setPassword("password");

        when(userRepository.existsByEmail("a@a.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnJwtResponse_withAdminFromDb() {
        LoginRequest req = new LoginRequest();
        req.setEmail("a@a.com");
        req.setPassword("password");

        UserDetailsImpl principal = UserDetailsImpl.builder()
                .id(1L).username("a@a.com").firstName("John").lastName("Doe").build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        when(jwtUtils.generateJwtToken(auth)).thenReturn("JWT");
        when(userRepository.findByEmail("a@a.com")).thenReturn(Optional.of(
                new User("a@a.com", "Doe", "John", "ENC", true)
        ));

        JwtResponse resp = authService.login(req);

        assertEquals("JWT", resp.getToken());
        assertEquals(1L, resp.getId());
        assertTrue(Boolean.TRUE.equals(resp.getAdmin()));
    }
}