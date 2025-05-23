package kr.ac.dankook.ace.healthy_meal_backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);
    private final Path rootLocation = Paths.get("/mnt/vol1/mysql_dir");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] DISALLOWED_EXTENSIONS = { ".exe", ".bat", ".sh" };

    @Override
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Cannot store empty file.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new StorageException("File size exceeds the allowed limit.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = "";

        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i).toLowerCase(); // Includes '.'
        }

        for (String ext : DISALLOWED_EXTENSIONS) {
            if (extension.equals(ext)) {
                throw new StorageException("File type not allowed: " + extension);
            }
        }

        String storedFilename = UUID.randomUUID().toString() + extension;

        try {
            if (originalFilename.contains("..")) {
                throw new StorageException("Cannot store file with relative path: " + originalFilename);
            }

            Path destinationFile = this.rootLocation.resolve(storedFilename).normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new StorageException("Attempt to store file outside the current directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            logger.info("File stored: {}", storedFilename);
            return storedFilename;

        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + originalFilename, e);
        }
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            logger.info("Storage initialized at: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage: " + rootLocation.toAbsolutePath(), e);
        }
    }

    @Override
    public Path load(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new StorageException("Cannot load file with empty name.");
        }
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void delete(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            logger.warn("Attempted to delete file with empty name.");
            return;
        }

        try {
            Path file = load(filename);
            if (!file.normalize().toAbsolutePath().startsWith(this.rootLocation.toAbsolutePath())) {
                logger.error("Attempted to delete file outside of storage: {}", filename);
                throw new StorageException("Cannot delete file outside current storage.");
            }

            Files.deleteIfExists(file);
            logger.info("File deleted: {}", filename);
        } catch (IOException e) {
            throw new StorageException("Failed to delete file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            FileSystemUtils.deleteRecursively(rootLocation);
            logger.info("All files deleted in storage: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new StorageException("Failed to delete all files", e);
        }
    }
}
