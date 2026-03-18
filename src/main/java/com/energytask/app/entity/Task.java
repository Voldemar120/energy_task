package com.energytask.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "task")
public class Task {

    // ============= СУЩЕСТВУЮЩИЕ ПОЛЯ =============

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;
    private String description;
    private String status;
    private boolean completed;


    // Добавьте поле archived с аннотацией Column
    @Column(name = "archived", nullable = false)
    private boolean archived = false; // По умолчанию false - задача не в архиве

    // Геттер и сеттер для archived
    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // ============= НОВЫЕ ПОЛЯ ДЛЯ КАНБАН-ДОСКИ =============

    /**
     * Тип колонки, в которой находится задача
     * Возможные значения:
     * - "todo" - нужно сделать
     * - "inprogress" - в процессе
     * - "done" - готово
     */
    private String columnType;

    /**
     * Позиция задачи внутри колонки (для сортировки)
     * Чем меньше число, тем выше задача в колонке
     */
    private Integer position;

    /**
     * Приоритет задачи
     * Возможные значения:
     * - "HIGH" - высокий (красный)
     * - "MEDIUM" - средний (желтый)
     * - "LOW" - низкий (зеленый)
     */
    private String priority;

    /**
     * Срок выполнения задачи (опционально)
     */
    private LocalDateTime dueDate;

    // ============= КОНСТРУКТОРЫ =============

    /**
     * Пустой конструктор (обязателен для JPA)
     * Здесь мы устанавливаем значения по умолчанию для новых полей
     */
    public Task() {
        this.createdAt = LocalDateTime.now(); // Дата создания - сейчас
        this.columnType = "todo";              // По умолчанию в колонку "Нужно сделать"
        this.position = 0;                      // По умолчанию позиция 0 (самая верхняя)
        this.priority = "MEDIUM";
        this.archived = false;
        // По умолчанию средний приоритет
    }

    /**
     * Полный конструктор со всеми полями
     */
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

    // ============= ГЕТТЕРЫ И СЕТТЕРЫ =============

    // --- Существующие геттеры/сеттеры ---

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

    // --- НОВЫЕ геттеры/сеттеры ---

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

    // ============= ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =============

    /**
     * Форматирует дату создания для отображения в интерфейсе
     */
    public String getFormattedCreatedAt() {
        if (createdAt != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            return createdAt.format(formatter);
        }
        return "";
    }

    /**
     * Форматирует дату выполнения для отображения в интерфейсе
     */
    public String getFormattedDueDate() {
        if (dueDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            return dueDate.format(formatter);
        }
        return "";
    }

    /**
     * Проверяет, просрочена ли задача
     */
    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDateTime.now()) && !completed;
    }

    /**
     * Возвращает CSS класс для приоритета (для стилей)
     */
    public String getPriorityClass() {
        if (priority == null) return "medium";
        return priority.toLowerCase();
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