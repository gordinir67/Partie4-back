package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class SessionServiceTest {

    @Mock SessionRepository sessionRepository;
    @Mock UserRepository userRepository;
    @InjectMocks SessionService sessionService;

    @Test
    void participate_shouldAddUser_andSave() {
        Session session = new Session().setId(1L).setUsers(new ArrayList<>());
        User user = new User().setId(2L);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        sessionService.participate(1L, 2L);

        assertEquals(1, session.getUsers().size());
        verify(sessionRepository).save(session);
    }

    @Test
    void participate_shouldThrow404_whenSessionMissing() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User().setId(2L)));

        assertThrows(ResponseStatusException.class, () -> sessionService.participate(1L, 2L));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void participate_shouldThrow400_whenAlreadyParticipating() {
        User user = new User().setId(2L);
        Session session = new Session().setId(1L).setUsers(new ArrayList<>());
        session.getUsers().add(user);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> sessionService.participate(1L, 2L));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void noLongerParticipate_shouldRemoveUser_andSave() {
        User user = new User().setId(2L);
        Session session = new Session().setId(1L).setUsers(new ArrayList<>());
        session.getUsers().add(user);

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        sessionService.noLongerParticipate(1L, 2L);

        assertTrue(session.getUsers().isEmpty());
        verify(sessionRepository).save(session);
    }
}