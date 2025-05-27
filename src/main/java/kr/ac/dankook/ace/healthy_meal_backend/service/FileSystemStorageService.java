package kr.ac.dankook.ace.healthy_meal_backend.service;

import kr.ac.dankook.ace.healthy_meal_backend.exception.StorageException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

    // 저장소 루트 위치를 클래스 내 상수로 정의 (예: "uploads" 폴더)
    // 실제 운영 환경에서는 이 경로를 적절히 수정해야 합니다.
    private static final String DEFAULT_STORAGE_LOCATION = "uploaded-files";
    private final Path rootLocation;

    // 생성자: StorageProperties 대신 상수 사용
    public FileSystemStorageService() {
        // DEFAULT_STORAGE_LOCATION이 비어있거나 null일 경우 기본값 "uploads" 사용
        String location = (DEFAULT_STORAGE_LOCATION == null || DEFAULT_STORAGE_LOCATION.trim().isEmpty())
                            ? "uploads"
                            : DEFAULT_STORAGE_LOCATION;
        this.rootLocation = Paths.get(location).toAbsolutePath().normalize();
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            System.out.println("Initialized storage at: " + rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new StorageException(
                    "Could not initialize storage location: " + rootLocation.toAbsolutePath(),
                    e,
                    StorageException.ErrorType.INITIALIZATION_FAILED
            );
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Failed to store empty file.", StorageException.ErrorType.EMPTY_FILE);
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i >= 0) {
            extension = originalFilename.substring(i);
        }
        String storedFilename = UUID.randomUUID().toString() + extension;

        try {
            if (originalFilename.contains("..")) {
                throw new StorageException(
                        "Cannot store file with relative path outside current directory: " + originalFilename,
                        StorageException.ErrorType.INVALID_FILENAME
                );
            }

            Path destinationFile = this.rootLocation.resolve(storedFilename).normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(this.rootLocation)) {
                throw new StorageException(
                        "Cannot store file outside current directory. Attempted path: " + destinationFile,
                        StorageException.ErrorType.INVALID_PATH
                );
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return storedFilename;
        } catch (IOException e) {
            throw new StorageException(
                    "Failed to store file " + originalFilename + ". " + e.getMessage(),
                    e,
                    StorageException.ErrorType.STORE_FAILED
            );
        }
    }

    @Override
    public Path load(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new StorageException("Filename cannot be null or empty for loading.", StorageException.ErrorType.INVALID_FILENAME);
        }
        return this.rootLocation.resolve(filename).normalize();
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                // StorageFileNotFoundException 대신 StorageException 사용
                throw new StorageException(
                        "Could not read file (not found or not readable): " + filename,
                        StorageException.ErrorType.FILE_NOT_FOUND
                );
            }
        } catch (MalformedURLException e) {
            // StorageFileNotFoundException 대신 StorageException 사용
            throw new StorageException(
                    "Could not read file due to malformed URL: " + filename,
                    e,
                    StorageException.ErrorType.LOAD_FAILED // 또는 FILE_NOT_FOUND, 상황에 따라
            );
        } catch (StorageException e) {
            // load() 메소드에서 발생한 StorageException을 그대로 전달하거나, 필요시 ErrorType 변경
             if (e.getErrorType() == StorageException.ErrorType.INVALID_FILENAME) {
                 throw e; // load에서 발생한 INVALID_FILENAME은 그대로 전달
             }
             // 그 외 load()에서 발생한 StorageException은 FILE_NOT_FOUND로 간주할 수 있음
             throw new StorageException(
                 "Could not load file as resource: " + filename + " (Cause: " + e.getMessage() + ")",
                 e,
                 StorageException.ErrorType.FILE_NOT_FOUND
             );
        }
    }

    @Override
    public void delete(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            System.err.println("Attempted to delete a file with null or empty filename.");
            // 필요시 예외 발생: throw new StorageException("Filename cannot be null or empty for deletion.", StorageException.ErrorType.INVALID_FILENAME);
            return;
        }
        try {
            Path file = load(filename);

            if (!file.toAbsolutePath().startsWith(this.rootLocation.toAbsolutePath())) {
                System.err.println("Attempted to delete a file outside the storage directory: " + filename);
                throw new StorageException(
                        "Cannot delete file outside storage directory: " + filename,
                        StorageException.ErrorType.INVALID_PATH
                );
            }
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new StorageException(
                    "Failed to delete file: " + filename + ". " + e.getMessage(),
                    e,
                    StorageException.ErrorType.DELETE_FAILED
            );
        } catch (StorageException e) {
            // load() 메소드에서 발생한 StorageException 처리
            throw new StorageException(
                "Failed to delete file: " + filename + " (Cause: " + e.getMessage() + ")",
                e,
                (e.getErrorType() == StorageException.ErrorType.INVALID_FILENAME) ? StorageException.ErrorType.INVALID_FILENAME : StorageException.ErrorType.DELETE_FAILED
            );
        }
    }

    @Override
    public void deleteAll() {
        try {
            Files.walk(this.rootLocation, 1)
                .filter(path -> !path.equals(this.rootLocation))
                .sorted((p1, p2) -> -p1.compareTo(p2))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Failed to delete " + path + ": " + e.getMessage());
                        // 개별 파일 삭제 실패 시 전체 작업 중단 없이 계속 진행할 수 있도록 처리
                        // 필요시 여기서 StorageException을 던져서 상위로 전파 가능
                    }
                });
        } catch (IOException e) {
            throw new StorageException(
                    "Could not delete all stored files. " + e.getMessage(),
                    e,
                    StorageException.ErrorType.DELETE_ALL_FAILED
            );
        }
    }
}
