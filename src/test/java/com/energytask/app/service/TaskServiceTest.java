package com.energytask.app.service;

import com.energytask.app.entity.Task;
import com.energytask.app.entity.User;
import com.energytask.app.repository.TaskRepository;
import com.energytask.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    // =========================================================
    // createTask
    // =========================================================

    @Test
    void createTask_shouldAssignUserAndSetDefaultColumnTypeAndPriority() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        // Override the no-arg constructor defaults so we exercise the null-check branches
        Task task = new Task();
        task.setColumnType(null);
        task.setPriority(null);
        task.setTitle("New Task");

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        taskService.createTask(task, "testUser");

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task saved = captor.getValue();

        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getColumnType()).isEqualTo("todo");
        assertThat(saved.getPriority()).isEqualTo("MEDIUM");
    }

    // =========================================================
    // updateTask
    // =========================================================

    @Test
    void updateTask_shouldUpdateTitleDescriptionCompletedColumnTypePriorityAndDueDate() {
        Task existing = new Task();
        existing.setId(1L);
        existing.setTitle("Old Title");
        existing.setDescription("Old Description");
        existing.setCompleted(false);
        existing.setColumnType("todo");
        existing.setPriority("LOW");
        existing.setDueDate(null);

        LocalDateTime newDueDate = LocalDateTime.of(2025, 12, 31, 23, 59);
        Task updated = new Task();
        updated.setId(1L);
        updated.setTitle("New Title");
        updated.setDescription("New Description");
        updated.setCompleted(true);
        updated.setColumnType("done");
        updated.setPriority("HIGH");
        updated.setDueDate(newDueDate);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        taskService.updateTask(updated);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("New Title");
        assertThat(saved.getDescription()).isEqualTo("New Description");
        assertThat(saved.isCompleted()).isTrue();
        assertThat(saved.getColumnType()).isEqualTo("done");
        assertThat(saved.getPriority()).isEqualTo("HIGH");
        assertThat(saved.getDueDate()).isEqualTo(newDueDate);
    }

    // =========================================================
    // toggleTaskStatus
    // =========================================================

    @Test
    void toggleTaskStatus_whenColumnIsDone_shouldSwitchToInProgressAndSetCompletedFalse() {
        Task task = new Task();
        task.setId(1L);
        task.setColumnType("done");
        task.setCompleted(true);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.toggleTaskStatus(1L);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task saved = captor.getValue();

        assertThat(saved.getColumnType()).isEqualTo("inprogress");
        assertThat(saved.isCompleted()).isFalse();
    }

    @Test
    void toggleTaskStatus_whenColumnIsInProgress_shouldSwitchToDoneAndSetCompletedTrue() {
        Task task = new Task();
        task.setId(2L);
        task.setColumnType("inprogress");
        task.setCompleted(false);

        when(taskRepository.findById(2L)).thenReturn(Optional.of(task));

        taskService.toggleTaskStatus(2L);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task saved = captor.getValue();

        assertThat(saved.getColumnType()).isEqualTo("done");
        assertThat(saved.isCompleted()).isTrue();
    }

    @Test
    void toggleTaskStatus_whenColumnIsTodo_shouldSwitchToInProgressAndSetCompletedFalse() {
        Task task = new Task();
        task.setId(3L);
        task.setColumnType("todo");
        task.setCompleted(false);

        when(taskRepository.findById(3L)).thenReturn(Optional.of(task));

        taskService.toggleTaskStatus(3L);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task saved = captor.getValue();

        assertThat(saved.getColumnType()).isEqualTo("inprogress");
        assertThat(saved.isCompleted()).isFalse();
    }

    // =========================================================
    // updateTaskColumn
    // =========================================================

    @Test
    void updateTaskColumn_whenNewColumnIsDone_shouldSetCompletedTrueAndUpdatePosition() {
        Task task = new Task();
        task.setId(1L);
        task.setColumnType("todo");
        task.setCompleted(false);
        task.setPosition(0);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.updateTaskColumn(1L, "done", 3);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task saved = captor.getValue();

        assertThat(saved.getColumnType()).isEqualTo("done");
        assertThat(saved.getPosition()).isEqualTo(3);
        assertThat(saved.isCompleted()).isTrue();
    }

    @Test
    void updateTaskColumn_whenNewColumnIsInProgress_shouldSetCompletedFalseAndUpdatePosition() {
        Task task = new Task();
        task.setId(2L);
        task.setColumnType("done");
        task.setCompleted(true);
        task.setPosition(1);

        when(taskRepository.findById(2L)).thenReturn(Optional.of(task));

        taskService.updateTaskColumn(2L, "inprogress", 0);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task saved = captor.getValue();

        assertThat(saved.getColumnType()).isEqualTo("inprogress");
        assertThat(saved.getPosition()).isEqualTo(0);
        assertThat(saved.isCompleted()).isFalse();
    }

    @Test
    void updateTaskColumn_whenNewColumnIsTodo_shouldSetCompletedFalseAndUpdatePosition() {
        Task task = new Task();
        task.setId(3L);
        task.setColumnType("inprogress");
        task.setCompleted(false);
        task.setPosition(2);

        when(taskRepository.findById(3L)).thenReturn(Optional.of(task));

        taskService.updateTaskColumn(3L, "todo", 1);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task saved = captor.getValue();

        assertThat(saved.getColumnType()).isEqualTo("todo");
        assertThat(saved.getPosition()).isEqualTo(1);
        assertThat(saved.isCompleted()).isFalse();
    }

    // =========================================================
    // archiveTask
    // =========================================================

    @Test
    void archiveTask_shouldSetArchivedToTrue() {
        Task task = new Task();
        task.setId(1L);
        task.setArchived(false);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.archiveTask(1L);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task saved = captor.getValue();

        assertThat(saved.isArchived()).isTrue();
    }
}
