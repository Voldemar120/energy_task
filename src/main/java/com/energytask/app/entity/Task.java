package com.energytask.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;
    private String description;
    private String status;
    private boolean completed;

    @Column(name = "archived", nullable = false)
    private boolean archived = false;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    @Transient
    private String attachmentsJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String columnType;
    private Integer position;
    private String priority;
    private LocalDateTime dueDate;

    // Конструкторы
    public Task() {
        this.createdAt = LocalDateTime.now();
        this.columnType = "todo";
        this.position = 0;
        this.priority = "MEDIUM";
        this.archived = false;
    }

    public Task(Long id, String title, String description, String status,
                boolean completed, User user, String columnType,
                Integer position, String priority, LocalDateTime dueDate, boolean archived) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.completed = completed;
        this.user = user;
        this.columnType = columnType != null ? columnType : "todo";
        this.position = position != null ? position : 0;
        this.priority = priority != null ? priority : "MEDIUM";
        this.archived = archived;
        this.dueDate = dueDate;
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и сеттеры для всех полей
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    // Геттеры и сеттеры для archived
    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    // Геттеры и сеттеры для attachments
    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    // Геттеры и сеттеры для attachmentsJson
    public String getAttachmentsJson() {
        return attachmentsJson;
    }

    public void setAttachmentsJson(String attachmentsJson) {
        this.attachmentsJson = attachmentsJson;
    }

    // Вспомогательные методы
    public String getFormattedCreatedAt() {
        if (createdAt != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            return createdAt.format(formatter);
        }
        return "";
    }

    public String getFormattedDueDate() {
        if (dueDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            return dueDate.format(formatter);
        }
        return "";
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDateTime.now()) && !completed;
    }

    public String getPriorityClass() {
        if (priority == null) return "medium";
        return priority.toLowerCase();
    }

    public String getFirstImageUrl() {
        if (attachments != null && !attachments.isEmpty()) {
            return attachments.get(0).getFilePath();
        }
        return null;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", columnType='" + columnType + '\'' +
                ", priority='" + priority + '\'' +
                ", completed=" + completed +
                '}';
    }
}