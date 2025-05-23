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
        if (file.isEmpty()) {
            throw new StorageException("빈 파일 저장 불가");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i); // .확장자 포함
        }

        // 파일 이름에 UUID를 추가하여 중복 방지 및 보안강화
        String storedFilename = UUID.randomUUID().toString() + extension;

        try {
            if (originalFilename.contains("..")) {
                // '../'가 포함된 파일 이름은 미허용
                throw new StorageException("상대경로로 접근은 불가합니다: " + originalFilename);
            }

            Path destinationFile = this.rootLocation.resolve(storedFilename).normalize().toAbsolutePath();

            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // rootLocation 외부의 경로에 저장하려는 시도 미허용
                throw new StorageException("현재 디렉토리 외부로의 접근은 불가합니다.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return storedFilename; // 저장된 파일 이름 (UUID + 확장자) 반환
        } 
        catch (IOException e) {
            throw new StorageException("파일 저장 실패: " + originalFilename, e);
        }
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            System.out.println("저장소 초기화 됨: " + rootLocation.toAbsolutePath());
        } 
        catch (IOException e) {
            throw new StorageException("저장소 초기화 불가: " + rootLocation.toAbsolutePath(), e);
        }
    }

    @Override
    public Path load(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new StorageException("빈 파일");
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
            } 
            else {
                throw new StorageFileNotFoundException("파일 읽기 실패: " + filename);
            }
        } 
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("파일 읽기에 실패했습니다: " + filename, e);
        }
    }

    @Override
    public void delete(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            System.err.println("빈 파일 이름");
            return; 
        }
        try {
            Path file = load(filename);
            // 파일이 실제로 rootLocation 내에 있는지 다시 한번 확인 
            if (!file.normalize().toAbsolutePath().startsWith(this.rootLocation.toAbsolutePath())) {
                 System.err.println("외부 파일 삭제 불가: " + filename);
                 throw new StorageException("현재 스토리지 외부 파일은 삭제 불가합니다");
            }
            Files.deleteIfExists(file);
        } 
        catch (IOException e) {
            // 파일이 사용 중이거나 다른 이유로 삭제할 수 없는 경우
            throw new StorageException("파일 삭제 실패: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            FileSystemUtils.deleteRecursively(rootLocation);
            
        } 
        catch (IOException e) {
            throw new StorageException("파일 일괄 삭제 실패: ", e);
        }
    }
}
