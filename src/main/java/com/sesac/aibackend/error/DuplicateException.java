package com.sesac.aibackend.error;

/**
 * 중복 자원 생성 시도 시 발생 (예: 이미 사용 중인 username).
 *
 * GlobalExceptionHandler에서 409로 매핑됩니다.
 */
public class DuplicateException extends RuntimeException {

    public DuplicateException(String message) {
        super(message);
    }
}
