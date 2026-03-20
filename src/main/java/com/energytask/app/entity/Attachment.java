package com.energytask.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;       // Оригинальное имя файла
    private String filePath;       // Путь к файлу на сервере (или URL)
    private String fileType;        // MIME-тип, например "image/png"
    private long fileSize;          // Размер в байтах

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    // --- Конструкторы ---
    public Attachment() {
        this.uploadedAt = LocalDateTime.now();
    }

    public Attachment(String fileName, String filePath, String fileType, long fileSize, Task task) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.task = task;
        this.uploadedAt = LocalDateTime.now();
    }

    // --- Геттеры и Сеттеры (сгенерируйте автоматически в IDEA) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
}

