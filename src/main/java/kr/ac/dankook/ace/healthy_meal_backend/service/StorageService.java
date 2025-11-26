package kr.ac.dankook.ace.healthy_meal_backend.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface StorageService {
    void init();

    String storeTemp(MultipartFile file);

    boolean existsInTemp(String filename);

    void storeRoot(String filename);

    Path load(String filename);

    Resource loadAsResource(String filename);

    void delete(String filename);

    void deleteAll();

    String convertImageToBase64(String fileName);
}
