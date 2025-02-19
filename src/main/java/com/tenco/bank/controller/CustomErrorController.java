package com.tenco.bank.controller;

import com.tenco.bank.handler.exception.RedirectException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @GetMapping("/error")
    public void handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                // 404 에러 일 경우 페이지 리턴
                // 여기서는 우리가 만들어 놓은 예외 처리를 활용 합니다.
                throw new RedirectException("잘못된 요청 입니다.", HttpStatus.NOT_FOUND);
            }
            // else if 구믄을 사용하여 상세 설정 가능
        }
    }
}