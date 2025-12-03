package com.example.to_dolist.data;

// SQLite에 매핑되는 할 일 모델
public class Task {
    private long id; // long 타입으로 변경 (SQLite ID는 long)
    private String title;
    private String description;
    private String category;
    private String dueTime; // RQ-0001: HH:MM 형식의 문자열로 저장
    private boolean isCompleted; // RQ-0005

    public Task() {
        // 기본 생성자
    }

    // 새 항목 생성을 위한 생성자
    public Task(String title, String description, String category, String dueTime, boolean isCompleted) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.dueTime = dueTime;
        this.isCompleted = isCompleted;
    }

    // --- Getters and Setters ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDueTime() { return dueTime; }
    public void setDueTime(String dueTime) { this.dueTime = dueTime; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}