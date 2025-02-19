package com.tenco.bank.dto;

import com.tenco.bank.repository.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

// SignUpFormDTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpDTO {

    private String username;
    private String password;
    private String fullname;
    // name 속성과 일치 시켜야 함 - 다중 처리는 MultipartFile[] 배열 활용
    private MultipartFile mFile;
    private String originFileName;
    private String uploadFileName;
	private String eMail;

    //DTO --> 변환
    public User toUser() {
        return User.builder()
                .username(this.username)
                .password(this.password)
                .fullname(this.fullname)
                .originFileName(originFileName)
                .uploadFileName(uploadFileName)
                .build();
    }
}