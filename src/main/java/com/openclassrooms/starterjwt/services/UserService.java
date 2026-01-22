package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void delete(Long id) {
        this.userRepository.deleteById(id);
    }

    /**
     * Supprime l'utilisateur seulement si l'email du demandeur correspond à l'email du compte.
     *
     * @return true si suppression effectuée, false si l'utilisateur n'existe pas
     * @throws SecurityException si le demandeur n'est pas autorisé
     */
    public boolean deleteIfOwner(Long id, String requesterEmail) {
        User user = this.userRepository.findById(id).orElse(null);
        if (user == null) {
            return false;
        }

        if (!Objects.equals(requesterEmail, user.getEmail())) {
            throw new SecurityException("Unauthorized");
        }

        this.userRepository.deleteById(id);
        return true;
    }

    public User findById(Long id) {
        return this.userRepository.findById(id).orElse(null);
    }
}
