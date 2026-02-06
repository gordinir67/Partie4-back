package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    @Test
    void deleteIfOwner_shouldDelete_whenOwnerMatches() {
        User u = new User().setId(10L).setEmail("owner@test.com");
        when(userRepository.findById(10L)).thenReturn(Optional.of(u));

        boolean deleted = userService.deleteIfOwner(10L, "owner@test.com");

        assertTrue(deleted);
        verify(userRepository).deleteById(10L);
    }

    @Test
    void deleteIfOwner_shouldThrowSecurityException_whenNotOwner() {
        User u = new User().setId(10L).setEmail("owner@test.com");
        when(userRepository.findById(10L)).thenReturn(Optional.of(u));

        assertThrows(SecurityException.class, () -> userService.deleteIfOwner(10L, "other@test.com"));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteIfOwner_shouldReturnFalse_whenUserMissing() {
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        boolean deleted = userService.deleteIfOwner(10L, "owner@test.com");

        assertFalse(deleted);
        verify(userRepository, never()).deleteById(anyLong());
    }
}