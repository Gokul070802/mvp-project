package com.mvp.mvp_backend.model.dto;

import java.time.LocalDateTime;

public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime dateTime;
    private String assignedPerson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TaskDTO(Long id, String title, String description, boolean completed,
                   LocalDateTime dateTime, String assignedPerson, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.dateTime = dateTime;
        this.assignedPerson = assignedPerson;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return completed; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getAssignedPerson() { return assignedPerson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
