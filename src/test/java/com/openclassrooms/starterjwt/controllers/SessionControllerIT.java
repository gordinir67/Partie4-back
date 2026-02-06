package com.openclassrooms.starterjwt.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.config.AbstractIntegrationTest;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SessionControllerIT extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired UserRepository userRepository;
    @Autowired TeacherRepository teacherRepository;
    @Autowired SessionRepository sessionRepository;

    Long teacherId;
    String token;

    @BeforeEach
    void setup() throws Exception {
        sessionRepository.deleteAll();
        userRepository.deleteAll();
        teacherRepository.deleteAll();

        Teacher t = teacherRepository.save(new Teacher().setFirstName("T").setLastName("L"));
        teacherId = t.getId();

        token = registerAndLogin("user@test.com", "password");
    }

    private String registerAndLogin(String email, String password) throws Exception {
        SignupRequest signup = new SignupRequest();
        signup.setEmail(email);
        signup.setFirstName("User");
        signup.setLastName("Test");
        signup.setPassword(password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword(password);

        String resp = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(resp);
        return json.get("token").asText();
    }

    @Test
    void listSessions_shouldReturn200_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/session")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void listSessions_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createSession_shouldCreate_andThenGetDetails() throws Exception {
        String body = """
        {
          "name": "Yoga",
          "date": %d,
          "teacher_id": %d,
          "description": "Relax"
        }
        """.formatted(new Date().getTime(), teacherId);

        String created = mockMvc.perform(post("/api/session")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Yoga"))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(get("/api/session/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Yoga"))
                .andExpect(jsonPath("$.teacher_id").value(teacherId));
    }

    @Test
    void createSession_shouldReturn400_whenMissingRequiredField() throws Exception {
        // manque description + date + teacher_id
        String body = """
        { "name": "Yoga" }
        """;

        mockMvc.perform(post("/api/session")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSession_shouldModify() throws Exception {
        // create first
        String createBody = """
        {
          "name": "Yoga",
          "date": %d,
          "teacher_id": %d,
          "description": "Relax"
        }
        """.formatted(new Date().getTime(), teacherId);

        String created = mockMvc.perform(post("/api/session")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(created).get("id").asLong();

        // update
        String updateBody = """
        {
          "id": %d,
          "name": "Yoga Updated",
          "date": %d,
          "teacher_id": %d,
          "description": "Relax Updated"
        }
        """.formatted(id, new Date().getTime(), teacherId);

        mockMvc.perform(put("/api/session/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Yoga Updated"))
                .andExpect(jsonPath("$.description").value("Relax Updated"));
    }

    @Test
    void updateSession_shouldReturn400_whenMissingRequiredField() throws Exception {
        // id ok mais manque date/teacher/description
        String body = """
        { "name": "Yoga Updated" }
        """;

        mockMvc.perform(put("/api/session/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSession_shouldDelete() throws Exception {
        // create
        String createBody = """
        {
          "name": "To delete",
          "date": %d,
          "teacher_id": %d,
          "description": "x"
        }
        """.formatted(new Date().getTime(), teacherId);

        String created = mockMvc.perform(post("/api/session")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(delete("/api/session/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/session/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
    @Test
    void getSession_shouldReturn404_whenSessionNotFound() throws Exception {
        mockMvc.perform(get("/api/session/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSession_shouldReturn404_whenSessionNotFound() throws Exception {
        mockMvc.perform(delete("/api/session/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}