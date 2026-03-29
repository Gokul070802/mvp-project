package com.mvp.mvp_backend.controller;

import com.mvp.mvp_backend.exception.ResourceNotFoundException;
import com.mvp.mvp_backend.model.Task;
import com.mvp.mvp_backend.model.User;
import com.mvp.mvp_backend.model.dto.DashboardDTO;
import com.mvp.mvp_backend.repository.TaskRepository;
import com.mvp.mvp_backend.repository.UserRepository;
import com.mvp.mvp_backend.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Dashboard", description = "User dashboard endpoints")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskService taskService;

    @Operation(summary = "Get dashboard summary", description = "Get a summary of user's tasks with completion statistics using optimized DB queries")
    @GetMapping
    public DashboardDTO getDashboard(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Use optimized DB counting instead of fetching all tasks
        long completed = taskService.getCompletedTaskCount(email);
        long pending = taskService.getPendingTaskCount(email);

        // Get all tasks for advanced filtering (ongoing, due, unassigned)
        List<Task> tasks = taskRepository.findByOwnerId(user.getId());

        long ongoing = tasks.stream()
                .filter(t -> !t.isCompleted() &&
                        t.getDateTime() != null &&
                        t.getDateTime().isAfter(LocalDateTime.now()))
                .count();

        long due = tasks.stream()
                .filter(t -> !t.isCompleted() &&
                        t.getDateTime() != null &&
                        t.getDateTime().isBefore(LocalDateTime.now()))
                .count();

        long unassigned = tasks.stream()
                .filter(t -> t.getAssignedPerson() == null || t.getAssignedPerson().isEmpty())
                .count();

        return new DashboardDTO(
                user.getName(),
                ongoing,
                completed,
                due,
                unassigned
        );
    }
}