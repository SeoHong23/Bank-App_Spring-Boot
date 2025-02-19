package com.tenco.bank.config;
import com.tenco.bank.handler.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;



@Configuration // IoC 대상 (스프링 부트 설정 클래스 이다)
// 내부에 메서드 동작을 통한 Bean 객체 생성시 사용
@RequiredArgsConstructor // final 시 사용
public class WebMvcConfig implements WebMvcConfigurer {

    // DI 처리
    @Autowired
    private final AuthInterceptor authInterceptor;
    // 코드 추가
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 우리가 만든 인터셉트 클래스를 등록할 수 있다.
    // 요청 올 때 마다 domain URI 검사를 할 예정
    // /account/xxx <- 으로 들어오는 도메인을 다 검사해!!!
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/account/**")
                .addPathPatterns("/auth/**");
    }

    // 코드 수정
    // 프로제트에서 사용할 가상 경로 정의 - /images/uploads/
    // 실제 서버 컴퓨터에 위치한 경로 정의
    // - file:///C:\\spring_upload\\bank\\upload/
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/uploads/**")
                .addResourceLocations("file:" + uploadDir);
//                .addResourceLocations("file:///C:\\spring_upload\\bank\\upload/");
    }


    // 코드 추가 부분
    // SpringSecurityCrypto 모듈에서 제공하는 BCryptPasswordEncoder 객체를
    // 어디에서든지 사용할 수 있도록 IoC 처리 합니다.
   @Bean // IoC 대상 - 싱글톤 처리
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

