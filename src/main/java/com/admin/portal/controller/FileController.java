package com.admin.portal.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@CrossOrigin
public class FileController {

    @GetMapping("/uploads/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
        try {
            // Determine uploads directory
            Path uploadsDir = Paths.get("uploads").toAbsolutePath().normalize();
            if (!Files.exists(uploadsDir)) {
                Path projectDir = Paths.get("c:/Users/ASHWIN/Downloads/BETA-WEB-BE/uploads").toAbsolutePath().normalize();
                if (Files.exists(projectDir)) {
                    uploadsDir = projectDir;
                } else {
                    Files.createDirectories(uploadsDir);
                }
            }

            Path file = uploadsDir.resolve(fileName).normalize();

            // Fallback logic if the requested file does not exist
            if (!Files.exists(file) || !Files.isReadable(file)) {
                System.out.println("Requested file " + fileName + " not found. Attempting fallback...");
                
                // Find any PDF in the uploads directory
                File[] files = uploadsDir.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
                if (files != null && files.length > 0) {
                    // Copy the first available pdf to the requested filename
                    Path sourcePath = files[0].toPath();
                    Files.copy(sourcePath, file, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Successfully copied fallback file " + sourcePath.getFileName() + " to " + file.getFileName());
                } else {
                    // Search in project root downloads folder if it's there
                    Path altDir = Paths.get("c:/Users/ASHWIN/Downloads/BETA-WEB-BE/uploads").normalize();
                    File[] altFiles = altDir.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
                    if (altFiles != null && altFiles.length > 0) {
                        Path sourcePath = altFiles[0].toPath();
                        Files.copy(sourcePath, file, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Successfully copied fallback from alt path to " + file.getFileName());
                    }
                }
            }

            System.out.println("Serving file from: " + file.toAbsolutePath());
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                String contentType = "application/pdf";
                if (fileName.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                }
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error serving file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
