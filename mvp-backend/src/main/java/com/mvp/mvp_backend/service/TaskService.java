package com.mvp.mvp_backend.service;

import com.mvp.mvp_backend.exception.ForbiddenException;
import com.mvp.mvp_backend.exception.ResourceNotFoundException;
import com.mvp.mvp_backend.model.Task;
import com.mvp.mvp_backend.model.User;
import com.mvp.mvp_backend.model.dto.PageResponse;
import com.mvp.mvp_backend.model.dto.TaskDTO;
import com.mvp.mvp_backend.model.dto.TaskRequest;
import com.mvp.mvp_backend.repository.TaskRepository;
import com.mvp.mvp_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public TaskDTO createTask(TaskRequest taskRequest, String userEmail) {
        User user = findUserByEmail(userEmail);

        Task task = new Task();
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setDateTime(taskRequest.getDateTime());
        task.setAssignedPerson(taskRequest.getAssignedPerson());
        task.setCompleted(Boolean.TRUE.equals(taskRequest.getCompleted()));
        task.setOwner(user);

        Task savedTask = taskRepository.save(task);
        return toDto(savedTask);
    }

    /**
     * Get paginated tasks for logged-in user with optional filtering and search
     */
    public PageResponse<TaskDTO> getTasksForUser(String userEmail, Pageable pageable, Boolean completed, String search) {
        User user = findUserByEmail(userEmail);
        Page<Task> taskPage;

        if (search != null && !search.isEmpty()) {
            if (completed != null) {
                taskPage = taskRepository.searchAndFilterByCompletion(user.getId(), search, completed, pageable);
            } else {
                taskPage = taskRepository.searchByTitleForOwner(user.getId(), search, pageable);
            }
        } else if (completed != null) {
            taskPage = taskRepository.findByOwnerIdAndCompleted(user.getId(), completed, pageable);
        } else {
            taskPage = taskRepository.findByOwnerId(user.getId(), pageable);
        }

        List<TaskDTO> taskDTOs = taskPage.getContent().stream().map(this::toDto).toList();
        return new PageResponse<>(taskDTOs, pageable.getPageNumber(), pageable.getPageSize(),
                taskPage.getTotalElements(), taskPage.getTotalPages());
    }

    /**
     * Get all tasks for user (for backward compatibility and dashboard)
     */
    public List<TaskDTO> getTasksForUser(String userEmail) {
        User user = findUserByEmail(userEmail);
        return taskRepository.findByOwnerId(user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    public TaskDTO completeTask(Long taskId, String userEmail) {
        Task task = findTaskById(taskId);
        verifyOwnership(task, userEmail);
        task.setCompleted(true);
        return toDto(taskRepository.save(task));
    }

    public TaskDTO updateTask(Long taskId, TaskRequest taskRequest, String userEmail) {
        Task task = findTaskById(taskId);
        verifyOwnership(task, userEmail);

        if (taskRequest.getTitle() != null) {
            task.setTitle(taskRequest.getTitle());
        }
        task.setDescription(taskRequest.getDescription());
        task.setDateTime(taskRequest.getDateTime());
        task.setAssignedPerson(taskRequest.getAssignedPerson());
        if (taskRequest.getCompleted() != null) {
            task.setCompleted(taskRequest.getCompleted());
        }

        return toDto(taskRepository.save(task));
    }

    public void deleteTask(Long taskId, String userEmail) {
        Task task = findTaskById(taskId);
        verifyOwnership(task, userEmail);
        taskRepository.delete(task);
    }

    public TaskDTO duplicateTask(Long taskId, String userEmail) {
        Task task = findTaskById(taskId);
        verifyOwnership(task, userEmail);

        Task duplicate = new Task();
        duplicate.setTitle(task.getTitle() + " (Copy)");
        duplicate.setDescription(task.getDescription());
        duplicate.setCompleted(false);
        duplicate.setDateTime(task.getDateTime());
        duplicate.setAssignedPerson(task.getAssignedPerson());
        duplicate.setOwner(task.getOwner());

        return toDto(taskRepository.save(duplicate));
    }

    /**
     * Get dashboard statistics for user
     */
    public long getCompletedTaskCount(String userEmail) {
        User user = findUserByEmail(userEmail);
        return taskRepository.countByOwnerIdAndCompleted(user.getId(), true);
    }

    /**
     * Get pending task count
     */
    public long getPendingTaskCount(String userEmail) {
        User user = findUserByEmail(userEmail);
        return taskRepository.countByOwnerIdAndCompleted(user.getId(), false);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private void verifyOwnership(Task task, String userEmail) {
        if (!task.getOwner().getEmail().equals(userEmail)) {
            throw new ForbiddenException("You are not allowed to modify this task");
        }
    }

    private TaskDTO toDto(Task task) {
        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getDateTime(),
                task.getAssignedPerson(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
