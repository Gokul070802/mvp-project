package com.mvp.mvp_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mvp.mvp_backend.model.Task;
import com.mvp.mvp_backend.model.User;
import com.mvp.mvp_backend.model.dto.AuthResponse;
import com.mvp.mvp_backend.model.dto.LoginRequest;
import com.mvp.mvp_backend.model.dto.TaskRequest;
import com.mvp.mvp_backend.repository.TaskRepository;
import com.mvp.mvp_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("taskuser@example.com");
        testUser.setPassword(passwordEncoder.encode("Pass@123"));
        testUser.setName("Task Test User");
        testUser.setRole("ROLE_USER");
        userRepository.save(testUser);

        // Login to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("taskuser@example.com");
        loginRequest.setPassword("Pass@123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                AuthResponse.class
        );
        jwtToken = authResponse.getToken();
    }

    @Test
    void testCreateTask() throws Exception {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTitle("Test Task");
        taskRequest.setDescription("This is a test task");
        taskRequest.setDateTime(LocalDateTime.now().plusDays(1));
        taskRequest.setAssignedPerson("John Doe");
        taskRequest.setCompleted(false);

        MvcResult result = mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("Test Task"));
    }

    @Test
    void testGetUserTasks() throws Exception {
        // Create task
        Task task = new Task();
        task.setTitle("User Task");
        task.setDescription("Task for user");
        task.setOwner(testUser);
        task.setCompleted(false);
        taskRepository.save(task);

        mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("User Task"));
    }

    @Test
    void testCompleteTask() throws Exception {
        Task task = new Task();
        task.setTitle("Complete Me");
        task.setDescription("This task needs to be completed");
        task.setOwner(testUser);
        task.setCompleted(false);
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(put("/api/tasks/" + savedTask.getId() + "/complete")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

        @Test
        void testSearchAndSortCombo() throws Exception {
                // Create tasks with varying titles and dateTimes
                Task t1 = new Task();
                t1.setTitle("Meeting with team");
                t1.setOwner(testUser);
                t1.setDateTime(LocalDateTime.now().plusDays(2));
                taskRepository.save(t1);

                Task t2 = new Task();
                t2.setTitle("Client meeting");
                t2.setOwner(testUser);
                t2.setDateTime(LocalDateTime.now().plusDays(1));
                taskRepository.save(t2);

                // search for 'meeting' and sort by dateTime desc
                MvcResult result = mockMvc.perform(get("/api/tasks")
                                .param("search", "meeting")
                                .param("sort", "dateTime,desc")
                                .param("page", "0")
                                .param("size", "10")
                                .header("Authorization", "Bearer " + jwtToken))
                                .andExpect(status().isOk())
                                .andReturn();

                String content = result.getResponse().getContentAsString();
                // first result should be Meeting with team (later date)
                assertTrue(content.indexOf("Meeting with team") < content.indexOf("Client meeting"));
        }

    @Test
    void testUpdateTask() throws Exception {
        Task task = new Task();
        task.setTitle("Update Me");
        task.setDescription("Original description");
        task.setOwner(testUser);
        task.setCompleted(false);
        Task savedTask = taskRepository.save(task);

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated description");
        updateRequest.setCompleted(false);

        mockMvc.perform(put("/api/tasks/" + savedTask.getId())
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void testDuplicateTask() throws Exception {
        Task task = new Task();
        task.setTitle("Duplicate Me");
        task.setDescription("Original");
        task.setOwner(testUser);
        task.setCompleted(false);
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(post("/api/tasks/" + savedTask.getId() + "/duplicate")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Duplicate Me (Copy)"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void testDeleteTask() throws Exception {
        Task task = new Task();
        task.setTitle("Delete Me");
        task.setOwner(testUser);
        task.setCompleted(false);
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(delete("/api/tasks/" + savedTask.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        assertFalse(taskRepository.existsById(savedTask.getId()));
    }

    @Test
    void testTaskOwnershipCheck() throws Exception {
        // Create another user
        User anotherUser = new User();
        anotherUser.setEmail("other@example.com");
        anotherUser.setPassword(passwordEncoder.encode("Pass@123"));
        anotherUser.setName("Other User");
        anotherUser.setRole("ROLE_USER");
        userRepository.save(anotherUser);

        // Create task for original user
        Task task = new Task();
        task.setTitle("Private Task");
        task.setOwner(testUser);
        task.setCompleted(false);
        Task savedTask = taskRepository.save(task);

        // Login as another user
        LoginRequest otherLogin = new LoginRequest();
        otherLogin.setEmail("other@example.com");
        otherLogin.setPassword("Pass@123");

        MvcResult otherLoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherLogin)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse otherAuth = objectMapper.readValue(
                otherLoginResult.getResponse().getContentAsString(),
                AuthResponse.class
        );

        // Try to modify other user's task - should fail
        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Hacked");

        mockMvc.perform(put("/api/tasks/" + savedTask.getId())
                .header("Authorization", "Bearer " + otherAuth.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testTasksRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isForbidden());
    }
}
