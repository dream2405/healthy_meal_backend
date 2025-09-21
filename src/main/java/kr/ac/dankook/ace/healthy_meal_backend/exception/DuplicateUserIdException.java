package kr.ac.dankook.ace.healthy_meal_backend.exception;

import lombok.Getter;

@Getter
public class DuplicateUserIdException extends RuntimeException {
    private final String userId;

    public DuplicateUserIdException(String userId) {
        super(String.format("아이디 '%s'는 이미 사용 중입니다", userId));
        this.userId = userId;
    }
}
