package kr.ac.dankook.ace.healthy_meal_backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {
    private final Path rootLocation = Paths.get("/mnt/vol1/mysql_dir");

    @Override
    public String store(MultipartFile file) {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public Path load(String filename) {
        return null;
    }

    @Override
    public Resource loadAsResource(String filename) {
        return null;
    }

    @Override
    public void delete(String filename) {

    }

    @Override
    public void deleteAll() {

    }
}
