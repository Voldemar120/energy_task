package com.energytask.app.service;

import com.energytask.app.entity.Attachment;
import com.energytask.app.entity.Task;
import com.energytask.app.entity.User;
import com.energytask.app.repository.TaskRepository;
import com.energytask.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Task> getAllTask() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    @Transactional
    public void createTask(Task task, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        task.setUser(user);
        // Устанавливаем значения по умолчанию для новых полей
        if (task.getColumnType() == null) {
            task.setColumnType("todo");
        }
        if (task.getPriority() == null) {
            task.setPriority("MEDIUM");
        }
        taskRepository.save(task);
    }

    @Transactional
    public void updateTask(Task task) {
        Task existingTask = taskRepository.findById(task.getId())
                .orElseThrow(() -> new RuntimeException("Task not found: " + task.getId()));

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setCompleted(task.isCompleted());
        existingTask.setColumnType(task.getColumnType());
        existingTask.setPriority(task.getPriority());
        existingTask.setDueDate(task.getDueDate());

        taskRepository.save(existingTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Transactional
    public void toggleTaskStatus(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found: " + id));

        // Переключаем колонку в зависимости от текущего состояния
        if ("done".equals(task.getColumnType())) {
            // Если было "Готово", отправляем в "В процессе"
            task.setColumnType("inprogress");
            task.setCompleted(false);
        } else if ("inprogress".equals(task.getColumnType())) {
            // Если было "В процессе", отправляем в "Готово"
            task.setColumnType("done");
            task.setCompleted(true);
        } else if ("todo".equals(task.getColumnType()) || task.getColumnType() == null) {
            // Если было "Нужно сделать", отправляем в "В процессе"
            task.setColumnType("inprogress");
            task.setCompleted(false);
        }

        taskRepository.save(task);
        System.out.println("Toggle статуса для задачи " + id + ": column=" + task.getColumnType());
    }

    // Новые методы для канбан-доски
    @Transactional
    public void updateTaskColumn(Long taskId, String newColumn, Integer position) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        // Сохраняем старый статус для логирования
        String oldColumn = task.getColumnType();

        // Обновляем колонку
        task.setColumnType(newColumn);
        task.setPosition(position);

        // Обновляем статус completed в соответствии с колонкой
        if ("done".equals(newColumn)) {
            task.setCompleted(true);
            System.out.println("Задача " + taskId + " перемещена в 'Готово', completed=true");
        } else if ("todo".equals(newColumn) || "inprogress".equals(newColumn)) {
            task.setCompleted(false);
            System.out.println("Задача " + taskId + " перемещена в '" + newColumn + "', completed=false");
        }

        taskRepository.save(task);

        // Дополнительная проверка
        System.out.println("Задача " + taskId + " обновлена: колонка " + oldColumn + " -> " + newColumn +
                ", completed=" + task.isCompleted());
    }

    @Transactional
    public void updateTaskPosition(Long taskId, Integer position) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        task.setPosition(position);
        taskRepository.save(task);
    }

    public List<Task> getTasksByUser_Username(String username) {
        return taskRepository.findTasksByUser_UsernameOrderByCreatedAtDesc(username);
    }

    // Для канбан-доски с сортировкой по позиции
    public List<Task> getTasksByUserForBoard(String username) {
        return taskRepository.findTasksByUser_UsernameOrderByPositionAsc(username);
    }

    // Добавьте в TaskService.java
    public List<Task> getArchivedTasksByUser(String username) {
        return taskRepository.findTasksByUser_UsernameAndArchivedTrue(username);
    }

    @Transactional
    public void archiveTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found: " + id));
        task.setArchived(true);
        taskRepository.save(task);
    }

    @Transactional
    public void restoreTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found: " + id));
        task.setArchived(false);
        taskRepository.save(task);
    }

    // Attachment
    @Transactional
    public void addAttachmentToTask(Long taskId, String fileUrl, String fileName, String fileType, long fileSize) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        Attachment attachment = new Attachment(fileName, fileUrl, fileType, fileSize, task);
        task.getAttachments().add(attachment);
        taskRepository.save(task);
        System.out.println("Вложение добавлено к задаче " + taskId + ": " + fileUrl);
    }
}
