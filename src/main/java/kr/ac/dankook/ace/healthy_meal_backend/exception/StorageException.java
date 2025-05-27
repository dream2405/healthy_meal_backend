package kr.ac.dankook.ace.healthy_meal_backend.exception;

import lombok.Getter;

@Getter
public class StorageException extends RuntimeException {

    private final ErrorType errorType; // 오류 유형을 저장하기 위한 필드

    // 다양한 오류 유형을 정의하는 enum
    public enum ErrorType {
        INITIALIZATION_FAILED,      // 저장소 초기화 실패
        EMPTY_FILE,                 // 빈 파일 저장 시도
        INVALID_FILENAME,           // 유효하지 않은 파일명 (예: 경로 조작 문자 포함)
        STORE_FAILED,               // 파일 저장 일반 실패
        LOAD_FAILED,                // 파일 로드 실패
        FILE_NOT_FOUND,             // 파일을 찾을 수 없음 (StorageFileNotFoundException 역할 통합)
        DELETE_FAILED,              // 파일 삭제 실패
        DELETE_ALL_FAILED,          // 모든 파일 삭제 실패
        INVALID_PATH,               // 유효하지 않은 경로 (예: 저장소 외부 접근 시도)
        UNKNOWN_ERROR               // 기타 알 수 없는 오류
    }

    // 생성자: 메시지와 오류 유형을 받음
    public StorageException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    // 생성자: 메시지, 원인 예외, 오류 유형을 받음
    public StorageException(String message, Throwable cause, ErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }
}
