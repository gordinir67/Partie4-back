package com.openclassrooms.starterjwt.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.config.AbstractIntegrationTest;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserControllerIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    private Long userId;
    private String token;
    private String email;

    @BeforeEach
    void setup() throws Exception {
        userRepository.deleteAll();

        email = uniqueEmail("me");
        token = registerAndLogin(email, "password123");

        // Récupérer l'id depuis la réponse login
        String loginJson = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, "password123");

        String resp = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(resp);
        userId = json.get("id").asLong();
    }

    private static String uniqueEmail(String prefix) {
        return prefix + "+" + System.nanoTime() + "@test.com";
    }

    private String registerAndLogin(String email, String password) throws Exception {
        // IMPORTANT: firstName doit respecter @Size(min=3)
        String signupJson = """
                {
                  "email": "%s",
                  "firstName": "%s",
                  "lastName": "%s",
                  "password": "%s"
                }
                """.formatted(email, "Meee", "User", password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson))
                .andExpect(status().isOk());

        String loginJson = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        String resp = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(resp);
        return json.get("token").asText();
    }

    @Test
    void account_shouldReturnUserInfo_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/user/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void account_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/user/" + userId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void account_shouldReturn404_whenUserNotFound() throws Exception {
        // Token valide, mais user inexistant => on couvre la branche "not found"
        mockMvc.perform(get("/api/user/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldWork_whenOwner() throws Exception {
        mockMvc.perform(delete("/api/user/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Après suppression, le token ne peut plus authentifier l'utilisateur (401),
        // donc on vérifie la suppression via la base.
        assertTrue(userRepository.findById(userId).isEmpty());
    }

    @Test
    void delete_shouldReturn404_whenUserNotFound() throws Exception {
        // Token valide, mais user inexistant => couvre la branche "not found" du delete
        mockMvc.perform(delete("/api/user/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn401_whenNotOwner() throws Exception {
        String otherEmail = uniqueEmail("other");
        String otherToken = registerAndLogin(otherEmail, "password123");

        mockMvc.perform(delete("/api/user/" + userId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isUnauthorized());
    }
}