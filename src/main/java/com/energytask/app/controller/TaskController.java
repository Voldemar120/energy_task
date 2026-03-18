package com.energytask.app.controller;

import com.energytask.app.service.TaskService;
import com.energytask.app.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/tasks")
public class TaskController {

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
        if (task.getId() == null) {
            taskService.createTask(task, username);
        } else {
            taskService.updateTask(task);
        }

        // Редирект на нужную страницу
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
}
