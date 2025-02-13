package com.tenco.bank.handler.exception;
// 사용자 정의 예외 클래스 만들기

import org.springframework.http.HttpStatus;

public class UnAuthorizedException extends RuntimeException {

    private HttpStatus status;
    // 예외 발생 했을때 --> Http 상태코드
    // 메세지 (어떤 예외 발생인지)

    public UnAuthorizedException (String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}