package kr.ac.dankook.ace.healthy_meal_backend.service;

import kr.ac.dankook.ace.healthy_meal_backend.exception.StorageException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
// import org.springframework.util.FileSystemUtils; 루트 디렉토리는 삭제 안함
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
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;


/**
 * 로컬 파일 시스템을 사용하여 파일을 저장하고 관리하는 서비스 구현체입니다.
 */
@Service
public class FileSystemStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);

    // 저장소 루트 위치를 클래스 내 상수로 정의.

    private final Path rootLocation;

    // 파일 크기 제한 = 100MB
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    // 허용되지 않는 파일 확장자 목록 (보안 강화)
    private static final String[] DISALLOWED_EXTENSIONS = {
            ".exe", ".dll", ".bat", ".sh", ".jar", ".com", ".cmd", ".vb", ".vbs", ".js", ".php", ".py", ".pl", ".rb"
            
    };

    /**
     * FileSystemStorageService 생성자입니다.
     * 저장소 루트 위치를 초기화합니다.
     */
    public FileSystemStorageService(@Value("${storage.location:uploads}") String location) {
        this.rootLocation = Paths.get(location).toAbsolutePath().normalize();
        logger.info("File system storage root location set to: {}", this.rootLocation);
    }

    /**
     * 서비스 초기화 시 저장소 루트 디렉토리를 생성합니다.
     */
    @Override
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            logger.info("Initialized storage at: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Could not initialize storage location: {}", rootLocation.toAbsolutePath(), e);
            throw new StorageException(
                    "Could not initialize storage location: " + rootLocation.toAbsolutePath(),
                    e,
                    StorageException.ErrorType.INITIALIZATION_FAILED
            );
        }
    }

    /**
     * 업로드된 파일을 저장소에 저장합니다.
     * 파일 크기 및 확장자 유효성 검사를 수행합니다.
     * @param file 저장할 MultipartFile 객체
     * @return 저장된 파일의 고유한 이름
     * @throws StorageException 파일 저장 중 오류 발생 시
     */
    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.warn("Attempted to store an empty file.");
            throw new StorageException("Failed to store empty file.", StorageException.ErrorType.EMPTY_FILE);
        }

        // 1. 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            logger.warn("File size ({}) exceeds the limit of {}MB for file: {}",
                    file.getSize(), (MAX_FILE_SIZE / (1024 * 1024)), file.getOriginalFilename());
            throw new StorageException(
                    "File size exceeds the limit of " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB.",
                    StorageException.ErrorType.FILE_SIZE_EXCEEDED
            );
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');

        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase(); // 확장자를 소문자로 변환하여 비교
        }

        // 2. 파일 확장자 검증
        final String finalExtension = extension; // 람다식 내부에서 사용하기 위해 final로 선언
        boolean isDisallowed = Arrays.asList(DISALLOWED_EXTENSIONS).contains(finalExtension);
        if (isDisallowed) {
            logger.warn("Attempted to upload a file with a disallowed extension ({}): {}",
                    extension, originalFilename);
            throw new StorageException(
                    "File extension (" + extension + ") is not allowed.",
                    StorageException.ErrorType.INVALID_EXTENSION
            );
        }

        // 저장될 파일명 생성 (UUID + 원본 확장자)
        String storedFilename = UUID.randomUUID().toString() + (dotIndex >= 0 ? originalFilename.substring(dotIndex) : "");

        try {
            // 파일명에 경로 조작 문자 포함 여부 확인
            if (originalFilename.contains("..")) {
                logger.warn("Attempted to store file with relative path in filename: {}", originalFilename);
                throw new StorageException(
                        "Cannot store file with relative path outside current directory: " + originalFilename,
                        StorageException.ErrorType.INVALID_FILENAME
                );
            }

            Path destinationFile = this.rootLocation.resolve(storedFilename).normalize().toAbsolutePath();

            // 최종 저장 경로가 루트 저장소 내에 있는지 확인 (보안)
            if (!destinationFile.getParent().equals(this.rootLocation)) {
                logger.error("Security alert: Attempted to store file outside the root storage directory. Target: {}", destinationFile);
                throw new StorageException(
                        "Cannot store file outside current directory. Attempted path: " + destinationFile,
                        StorageException.ErrorType.INVALID_PATH
                );
            }

            // 파일 복사
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Successfully stored file {} as {}", originalFilename, storedFilename);
            }
            return storedFilename;
        } catch (IOException e) {
            logger.error("Failed to store file {}: {}", originalFilename, e.getMessage(), e);
            throw new StorageException(
                    "Failed to store file " + originalFilename + ". " + e.getMessage(),
                    e,
                    StorageException.ErrorType.STORE_FAILED
            );
        }
    }

    /**
     * 지정된 파일명의 Path 객체를 반환합니다.
     * @param filename 로드할 파일의 이름
     * @return 파일의 Path 객체
     * @throws StorageException 파일명이 유효하지 않을 경우
     */
    @Override
    public Path load(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            logger.warn("Attempted to load a file with null or empty filename.");
            throw new StorageException("Filename cannot be null or empty for loading.", StorageException.ErrorType.INVALID_FILENAME);
        }
        return this.rootLocation.resolve(filename).normalize();
    }

    /**
     * 지정된 파일을 Resource 객체로 로드합니다.
     * @param filename 로드할 파일의 이름
     * @return Resource 객체
     * @throws StorageException 파일을 찾을 수 없거나 읽을 수 없을 때, 또는 URL 형식이 잘못되었을 때
     */
    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                logger.debug("Successfully loaded file {} as resource.", filename);
                return resource;
            } else {
                logger.warn("Could not read file (not found or not readable): {}", filename);
                throw new StorageException(
                        "Could not read file (not found or not readable): " + filename,
                        StorageException.ErrorType.FILE_NOT_FOUND
                );
            }
        } catch (MalformedURLException e) {
            logger.error("Could not read file due to malformed URL for filename {}: {}", filename, e.getMessage(), e);
            throw new StorageException(
                    "Could not read file due to malformed URL: " + filename,
                    e,
                    StorageException.ErrorType.LOAD_FAILED
            );
        } catch (StorageException e) {
            // load()에서 발생한 StorageException (INVALID_FILENAME)을 그대로 전파하거나, FILE_NOT_FOUND로 처리
            if (e.getErrorType() == StorageException.ErrorType.INVALID_FILENAME) {
                throw e;
            }
            logger.warn("Failed to load file {} as resource: {}", filename, e.getMessage());
            throw new StorageException(
                 "Could not load file as resource: " + filename + " (Details: " + e.getMessage() + ")",
                 e, // 원인 예외 전달
                 StorageException.ErrorType.FILE_NOT_FOUND // 일반적으로 파일을 찾을 수 없는 상황으로 간주
            );
        }
    }

    /**
     * 지정된 파일을 삭제합니다.
     * @param filename 삭제할 파일의 이름
     * @throws StorageException 파일 삭제 중 오류 발생 시
     */
    @Override
    public void delete(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            logger.warn("Attempted to delete a file with null or empty filename.");
            return; // 또는 예외 발생
        }
        try {
            Path fileToDelete = load(filename);

            // 삭제하려는 파일이 루트 저장소 내에 있는지 확인 (보안)
            if (!fileToDelete.toAbsolutePath().startsWith(this.rootLocation.toAbsolutePath())) {
                logger.error("Security alert: Attempted to delete a file outside the root storage directory: {}", filename);
                throw new StorageException(
                        "Cannot delete file outside storage directory: " + filename,
                        StorageException.ErrorType.INVALID_PATH
                );
            }
            Files.deleteIfExists(fileToDelete);
            logger.info("Successfully deleted file: {}", filename);
        } catch (IOException e) {
            logger.error("Failed to delete file {}: {}", filename, e.getMessage(), e);
            throw new StorageException(
                    "Failed to delete file: " + filename + ". " + e.getMessage(),
                    e,
                    StorageException.ErrorType.DELETE_FAILED
            );
        } catch (StorageException e) {
            // load()에서 발생한 StorageException 처리
             logger.warn("Failed to delete file {} due to prior load error: {}", filename, e.getMessage());
            throw new StorageException(
                "Failed to delete file: " + filename + " (Details: " + e.getMessage() + ")",
                e, // 원인 예외 전달
                (e.getErrorType() == StorageException.ErrorType.INVALID_FILENAME) ?
                    StorageException.ErrorType.INVALID_FILENAME : StorageException.ErrorType.DELETE_FAILED
            );
        }
    }

    /**
     * 저장소의 모든 파일을 삭제합니다.
     * 주의: 이 작업은 되돌릴 수 없습니다.
     * @throws StorageException 모든 파일 삭제 중 오류 발생 시
     */
    @Override
    public void deleteAll() {
        logger.info("Attempting to delete all files in storage directory: {}", this.rootLocation);
        try {
            // FileSystemUtils.deleteRecursively(this.rootLocation); // 루트 디렉토리 자체도 삭제
            // init(); // 루트 디렉토리 재생성

            // 루트 디렉토리 내의 파일 및 하위 디렉토리만 삭제 (루트 디렉토리는 유지)
            Files.walk(this.rootLocation)
                .filter(path -> !path.equals(this.rootLocation)) // 루트 디렉토리 자체는 제외
                .sorted((p1, p2) -> -p1.compareTo(p2)) // 하위 항목부터 삭제 (디렉토리 내 파일 먼저 삭제)
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        logger.debug("Deleted: {}", path);
                    } catch (IOException e) {
                        logger.error("Failed to delete {}: {}", path, e.getMessage(), e);
                        // 개별 파일/디렉토리 삭제 실패 시 전체 작업을 중단하지 않고 계속 진행
                        // 필요에 따라 여기서 예외를 던져 상위로 전파할 수 있음
                    }
                });
            logger.info("Successfully deleted all contents of storage directory: {}", this.rootLocation);
        } catch (IOException e) {
            logger.error("Could not delete all stored files from {}: {}", this.rootLocation, e.getMessage(), e);
            throw new StorageException(
                    "Could not delete all stored files. " + e.getMessage(),
                    e,
                    StorageException.ErrorType.DELETE_ALL_FAILED
            );
        }
    }

    @Override
    public String convertImageToBase64(String fileName) {
        try {
            byte[] imageBytes = Files.readAllBytes(load(fileName));
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
