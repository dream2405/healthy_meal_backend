package kr.ac.dankook.ace.healthy_meal_backend.exception;

import lombok.Getter;

@Getter
public class DuplicateUserEmailException extends RuntimeException {
    private final String email;

    public DuplicateUserEmailException(String email) {
        super(String.format("이메일 '%s'는 이미 사용 중입니다", email));
        this.email = email;
    }
}
