package kr.ac.dankook.ace.healthy_meal_backend.exception;

import lombok.Getter;

/**
 * 파일 저장 및 관리 작업 중 발생하는 예외를 위한 클래스입니다.
 * 다양한 오류 유형을 ErrorType enum으로 구분합니다.
 */
@Getter
public class StorageException extends RuntimeException {

    private final ErrorType errorType; // 오류 유형을 저장하는 필드

    /**
     * 다양한 스토리지 관련 오류 유형을 정의합니다.
     */
    public enum ErrorType {
        INITIALIZATION_FAILED,      // 저장소 초기화 실패
        EMPTY_FILE,                 // 빈 파일 저장 시도
        FILE_SIZE_EXCEEDED,         // 파일 크기 제한 초과
        INVALID_EXTENSION,          // 허용되지 않는 파일 확장자
        INVALID_FILENAME,           // 유효하지 않은 파일명 (예: 경로 조작 문자 포함)
        STORE_FAILED,               // 파일 저장 일반 실패
        LOAD_FAILED,                // 파일 로드 실패
        FILE_NOT_FOUND,             // 파일을 찾을 수 없음 (StorageFileNotFoundException 역할 통합)
        DELETE_FAILED,              // 파일 삭제 실패
        DELETE_ALL_FAILED,          // 모든 파일 삭제 실패
        INVALID_PATH,               // 유효하지 않은 경로 (예: 저장소 외부 접근 시도)
        UNKNOWN_ERROR               // 기타 알 수 없는 오류
    }

    /**
     * 지정된 메시지와 오류 유형으로 StorageException을 생성합니다.
     * @param message 오류 메시지
     * @param errorType 오류 유형 (ErrorType enum)
     */
    public StorageException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    /**
     * 지정된 메시지, 원인 예외, 오류 유형으로 StorageException을 생성합니다.
     * @param message 오류 메시지
     * @param cause 원인이 되는 예외
     * @param errorType 오류 유형 (ErrorType enum)
     */
    public StorageException(String message, Throwable cause, ErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }
}
