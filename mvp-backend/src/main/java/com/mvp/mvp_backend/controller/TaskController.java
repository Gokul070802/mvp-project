package com.mvp.mvp_backend.controller;

import com.mvp.mvp_backend.model.dto.PageResponse;
import com.mvp.mvp_backend.model.dto.TaskDTO;
import com.mvp.mvp_backend.model.dto.TaskRequest;
import com.mvp.mvp_backend.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "Tasks", description = "Task management endpoints")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Operation(summary = "Create a new task", description = "Add a new task for the logged-in user")
    @PostMapping
    public TaskDTO createTask(@RequestBody @Valid TaskRequest taskRequest, Authentication authentication) {
        return taskService.createTask(taskRequest, authentication.getName());
    }

    @Operation(summary = "Get my tasks", description = "Retrieve paginated tasks for the logged-in user with optional filtering and search")
    @GetMapping
    public PageResponse<TaskDTO> getMyTasks(
            Authentication authentication,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field and direction (e.g., 'dateTime,desc')", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @Parameter(description = "Filter by completion status (true/false)")
            @RequestParam(required = false) Boolean completed,
            @Parameter(description = "Search by task title")
            @RequestParam(required = false) String search
    ) {
        // support multiple sort criteria: e.g. "dateTime,desc;title,asc" or single "createdAt,desc"
        Sort sortSpec;
        if (sort.contains(";")) {
            String[] parts = sort.split(";");
            Sort.Order[] orders = java.util.Arrays.stream(parts)
                .map(s -> s.split(","))
                .map(arr -> new Sort.Order(
                    arr.length > 1 ? Sort.Direction.fromString(arr[1]) : Sort.Direction.DESC,
                    arr[0]
                ))
                .toArray(Sort.Order[]::new);
            sortSpec = Sort.by(orders);
        } else {
            String[] sortParts = sort.split(",");
            Sort.Direction direction = Sort.Direction.fromString(sortParts.length > 1 ? sortParts[1] : "DESC");
            sortSpec = Sort.by(direction, sortParts[0]);
        }
        Pageable pageable = PageRequest.of(page, size, sortSpec);
        
        return taskService.getTasksForUser(authentication.getName(), pageable, completed, search);
    }

    @Operation(summary = "Mark task as complete", description = "Mark a task as completed")
    @PutMapping("/{id}/complete")
    public TaskDTO markCompleted(@PathVariable Long id, Authentication authentication) {
        return taskService.completeTask(id, authentication.getName());
    }

    @Operation(summary = "Update task", description = "Update task details")
    @PutMapping("/{id}")
    public TaskDTO updateTask(@PathVariable Long id,
                              @RequestBody TaskRequest updatedTask,
                              Authentication authentication) {
        return taskService.updateTask(id, updatedTask, authentication.getName());
    }

    @Operation(summary = "Delete task", description = "Delete a task by ID")
    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Long id, Authentication authentication) {
        taskService.deleteTask(id, authentication.getName());
        return "Task deleted successfully";
    }

    @Operation(summary = "Duplicate task", description = "Create a copy of an existing task")
    @PostMapping("/{id}/duplicate")
    public TaskDTO duplicateTask(@PathVariable Long id, Authentication authentication) {
        return taskService.duplicateTask(id, authentication.getName());
    }
}
