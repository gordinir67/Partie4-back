package com.openclassrooms.starterjwt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.config.AbstractIntegrationTest;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc // garde la sécurité Spring active
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void register_shouldCreateAccount() throws Exception {
        SignupRequest req = new SignupRequest();
        req.setEmail("a@a.com");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("registered successfully")));
    }

    @Test
    void register_shouldReturn400_whenMissingRequiredField() throws Exception {
        // manque password + lastName etc.
        SignupRequest req = new SignupRequest();
        req.setEmail("a@a.com");
        req.setFirstName("John");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldSucceed_andReturnJwt_andIsAdminFalseByDefault() throws Exception {
        // register
        SignupRequest signup = new SignupRequest();
        signup.setEmail("u@u.com");
        signup.setFirstName("User");
        signup.setLastName("Test");
        signup.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk());

        // login
        LoginRequest login = new LoginRequest();
        login.setEmail("u@u.com");
        login.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    void login_shouldReturn401_whenBadPassword() throws Exception {
        // register
        SignupRequest signup = new SignupRequest();
        signup.setEmail("u@u.com");
        signup.setFirstName("User");
        signup.setLastName("Test");
        signup.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk());

        // bad login
        LoginRequest login = new LoginRequest();
        login.setEmail("u@u.com");
        login.setPassword("WRONG");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturn400_whenMissingRequiredField() throws Exception {
        // email manquant (NotBlank)
        String body = """
        {"password":"password"}
        """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}