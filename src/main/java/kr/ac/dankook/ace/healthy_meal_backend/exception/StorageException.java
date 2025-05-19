package kr.ac.dankook.ace.healthy_meal_backend.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageException extends RuntimeException {
    private String location = "upload-dir";
}
