package com.energytask.app.controller;

import com.energytask.app.entity.Task;
import com.energytask.app.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/board")
public class BoardController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public String board(Model model, Principal principal) {
        String username = principal.getName();
        // Используем новый метод с сортировкой по позиции
        List<Task> tasks = taskService.getTasksByUserForBoard(username);

        Map<String, List<Task>> groupedTasks = tasks.stream()
                .collect(Collectors.groupingBy(
                        task -> task.getColumnType() != null ? task.getColumnType() : "todo"
                ));

        model.addAttribute("todoTasks", groupedTasks.getOrDefault("todo", List.of()));
        model.addAttribute("inProgressTasks", groupedTasks.getOrDefault("inprogress", List.of()));
        model.addAttribute("doneTasks", groupedTasks.getOrDefault("done", List.of()));

        return "board";
    }

    @PostMapping("/update-column")
    @ResponseBody
    public ResponseEntity<?> updateColumn(@RequestBody Map<String, Object> payload) {
        try {
            Long taskId = Long.valueOf(payload.get("taskId").toString());
            String newColumn = payload.get("column").toString();
            Integer newPosition = Integer.valueOf(payload.get("position").toString());

            taskService.updateTaskColumn(taskId, newColumn, newPosition);

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Задача успешно перемещена"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}