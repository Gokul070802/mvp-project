package com.mvp.mvp_backend.model.dto;

public class DashboardDTO {

    private String name;
    private long ongoing;
    private long completed;
    private long due;
    private long unassigned;

    public DashboardDTO(String name, long ongoing, long completed, long due, long unassigned) {
        this.name = name;
        this.ongoing = ongoing;
        this.completed = completed;
        this.due = due;
        this.unassigned = unassigned;
    }

    public String getName() { return name; }
    public long getOngoing() { return ongoing; }
    public long getCompleted() { return completed; }
    public long getDue() { return due; }
    public long getUnassigned() { return unassigned; }
}