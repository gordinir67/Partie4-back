package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public SessionService(SessionRepository sessionRepository, UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    public Session create(Session session) {
        return this.sessionRepository.save(session);
    }

    public void delete(Long id) {
        this.sessionRepository.deleteById(id);
    }

    /**
     * Supprime une session si elle existe.
     *
     * @return true si supprimée, false si introuvable
     */
    public boolean deleteIfExists(Long id) {
        Session session = this.sessionRepository.findById(id).orElse(null);
        if (session == null) {
            return false;
        }
        this.sessionRepository.deleteById(id);
        return true;
    }

    public List<Session> findAll() {
        return this.sessionRepository.findAll();
    }

    public Session getById(Long id) {
        return this.sessionRepository.findById(id).orElse(null);
    }

    public Session update(Long id, Session session) {
        session.setId(id);
        return this.sessionRepository.save(session);
    }

    public void participate(Long id, Long userId) {
        Session session = this.sessionRepository.findById(id).orElse(null);
        User user = this.userRepository.findById(userId).orElse(null);

        if (session == null || user == null) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        boolean alreadyParticipate = session.getUsers().stream()
                .anyMatch(o -> o.getId().equals(userId));

        if (alreadyParticipate) {
            throw new ResponseStatusException(BAD_REQUEST);
        }

        session.getUsers().add(user);
        this.sessionRepository.save(session);
    }

    public void noLongerParticipate(Long id, Long userId) {
        Session session = this.sessionRepository.findById(id).orElse(null);

        if (session == null) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        boolean alreadyParticipate = session.getUsers().stream()
                .anyMatch(o -> o.getId().equals(userId));

        if (!alreadyParticipate) {
            throw new ResponseStatusException(BAD_REQUEST);
        }

        session.setUsers(
                session.getUsers().stream()
                        .filter(user -> !user.getId().equals(userId))
                        .collect(Collectors.toList())
        );

        this.sessionRepository.save(session);
    }
}
