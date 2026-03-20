package com.energytask.app.controller;

import com.energytask.app.entity.Attachment;
import com.energytask.app.service.TaskService;
import com.energytask.app.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private static final Logger LOGGER = Logger.getLogger(TaskController.class.getName());

    @Autowired
    private TaskService taskService;

    @GetMapping
    public String index(Model model, Principal principal) {
        String username = principal.getName();
        model.addAttribute("tasks", taskService.getTasksByUser_Username(username));
        return "task-list";
    }

    @GetMapping("/new")
    public String newTask(Model model,
                          @RequestParam(required = false) String redirect) {
        model.addAttribute("task", new Task());
        // Добавляем redirect в модель, чтобы использовать в шаблоне
        if (redirect != null) {
            model.addAttribute("redirect", redirect);
        }
        return "task-form";
    }

    @PostMapping("/save")
    public String saveTask(@ModelAttribute("task") Task task,
                           @RequestParam(required = false) String redirect,
                           Principal principal) {
        String username = principal.getName();

        System.out.println("=== СОХРАНЕНИЕ ЗАДАЧИ ===");
        System.out.println("ID задачи: " + task.getId());
        System.out.println("Название: " + task.getTitle());
        System.out.println("Attachments JSON из объекта: " + task.getAttachmentsJson());

        Long taskId = task.getId();

        if (taskId == null) {
            taskService.createTask(task, username);
            taskId = task.getId();
        } else {
            taskService.updateTask(task);
        }

        // Обработка вложений из поля объекта
        String attachmentsJson = task.getAttachmentsJson();
        if (attachmentsJson != null && !attachmentsJson.isEmpty() && taskId != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<String> attachmentUrls = mapper.readValue(attachmentsJson,
                        mapper.getTypeFactory().constructCollectionType(List.class, String.class));

                System.out.println("Получены URL вложений: " + attachmentUrls);

                for (String url : attachmentUrls) {
                    String fileName = url.substring(url.lastIndexOf("/") + 1);
                    // Проверяем, существует ли уже такое вложение
                    boolean exists = taskService.getTaskById(taskId).getAttachments().stream()
                            .anyMatch(a -> a.getFilePath().equals(url));

                    if (!exists) {
                        taskService.addAttachmentToTask(taskId, url, fileName, "image/jpeg", 0);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if ("board".equals(redirect)) {
            return "redirect:/board";
        }
        return "redirect:/tasks";
    }

    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable("id") Long id) {
        taskService.deleteTask(id);
        return "redirect:/tasks";
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable("id") Long id) {
        taskService.toggleTaskStatus(id);
        return "redirect:/tasks";
    }

    // ЕДИНСТВЕННЫЙ МЕТОД editTask
    @GetMapping("/edit/{id}")
    public String editTask(@PathVariable("id") Long id,
                           Model model,
                           @RequestParam(required = false) String redirect) {
        model.addAttribute("task", taskService.getTaskById(id));
        if (redirect != null) {
            model.addAttribute("redirect", redirect);
        }
        return "task-form";
    }

    @GetMapping("/archive/{id}")
    public String archiveTask(@PathVariable("id") Long id) {
        taskService.archiveTask(id);
        return "redirect:/tasks";
    }

    @GetMapping("/task/{id}")
    @ResponseBody
    public ResponseEntity<?> getTask(@PathVariable Long id) {
        try {
            Task task = taskService.getTaskById(id);
            if (task == null) {
                return ResponseEntity.notFound().build();
            }

            String dueDateFormatted = null;
            if (task.getDueDate() != null) {
                dueDateFormatted = task.getDueDate().toString().substring(0, 16);
            }

            // Используем List<Map<String, Object>> с явным созданием HashMap
            List<Map<String, Object>> attachmentsInfo = new java.util.ArrayList<>();
            for (Attachment att : task.getAttachments()) {
                Map<String, Object> attachmentMap = new java.util.HashMap<>();
                attachmentMap.put("id", att.getId());
                attachmentMap.put("url", att.getFilePath());
                attachmentMap.put("fileName", att.getFileName());
                attachmentMap.put("fileType", att.getFileType());
                attachmentsInfo.add(attachmentMap);
            }

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", task.getId());
            response.put("title", task.getTitle() != null ? task.getTitle() : "");
            response.put("description", task.getDescription() != null ? task.getDescription() : "");
            response.put("priority", task.getPriority() != null ? task.getPriority() : "MEDIUM");
            response.put("columnType", task.getColumnType() != null ? task.getColumnType() : "todo");
            response.put("completed", task.isCompleted());
            response.put("dueDate", dueDateFormatted);
            response.put("attachments", attachmentsInfo);

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            LOGGER.severe("Ошибка при загрузке задачи: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
