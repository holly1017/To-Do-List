package com.example.to_dolist.data;

// SQLite에 매핑되는 할 일 모델
public class TodoItem {
    private long id; // long 타입으로 변경 (SQLite ID는 long)
    private String title;
    private String category_id;
    private String dueTime; // RQ-0001: HH:MM 형식의 문자열로 저장
    private boolean isCompleted; // RQ-0005

    public TodoItem() {
        // 기본 생성자
    }

    // 새 항목 생성을 위한 생성자
    public TodoItem(String title, String category_id, String dueTime, boolean isCompleted) {
        this.title = title;
        this.category_id = category_id;
        this.dueTime = dueTime;
        this.isCompleted = isCompleted;
    }

    // --- Getters and Setters ---
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategoryId() { return category_id; }
    public void setCategoryId(String category) { this.category_id = category_id; }
    public String getDueTime() { return dueTime; }
    public void setDueTime(String dueTime) { this.dueTime = dueTime; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}