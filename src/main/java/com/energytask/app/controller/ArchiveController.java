package com.energytask.app.controller;

import com.energytask.app.entity.Task;
import com.energytask.app.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/archive")
public class ArchiveController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public String archive(Model model, Principal principal) {
        String username = principal.getName();
        List<Task> archivedTasks = taskService.getArchivedTasksByUser(username);
        model.addAttribute("tasks", archivedTasks);
        return "archive";
    }

    @GetMapping("/restore/{id}")
    public String restoreTask(@PathVariable("id") Long id) {
        taskService.restoreTask(id);
        return "redirect:/archive";
    }
}
