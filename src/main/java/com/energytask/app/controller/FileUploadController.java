package com.energytask.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/api/uploads")
public class FileUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping(consumes = "multipart/form-data")
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "_csrf", required = false) String csrfToken) {
        try {
            // CSRF токен уже проверен Spring Security
            System.out.println("CSRF токен получен: " + (csrfToken != null ? "да" : "нет"));

            // Проверка на пустой файл
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Файл пуст"));
            }

            // Проверка типа файла (только изображения)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(Map.of("error", "Можно загружать только изображения"));
            }

            // Генерация безопасного имени файла
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String safeFileName = UUID.randomUUID() + extension;

            // Создание директории, если её нет
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // Сохранение файла
            Path targetLocation = uploadPath.resolve(safeFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Формирование URL для доступа к файлу
            String fileUrl = "/uploads/" + safeFileName;

            // Возвращаем успешный ответ с URL
            return ResponseEntity.ok().body(Map.of(
                    "url", fileUrl,
                    "fileName", originalFilename,
                    "size", file.getSize()
            ));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Не удалось сохранить файл"));
        }
    }
}
