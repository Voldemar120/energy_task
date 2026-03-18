package com.energytask.app.repository;

import com.energytask.app.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findTasksByUser_Username(String username);

    // Сортировка по дате создания (для таблицы)
    List<Task> findTasksByUser_UsernameOrderByCreatedAtDesc(String username);

    // Сортировка по позиции (для канбан-доски)
    List<Task> findTasksByUser_UsernameOrderByPositionAsc(String username);

    // Добавьте в TaskRepository.java
    List<Task> findTasksByUser_UsernameAndArchivedTrue(String username);
    List<Task> findTasksByUser_UsernameAndArchivedFalse(String username);
}
