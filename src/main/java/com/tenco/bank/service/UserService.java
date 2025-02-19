package com.tenco.bank.service;

import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.UserRepository;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.utils.Define;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor // 코드 추가 부분
public class UserService {

    // 코드 추가 부분
    @Autowired // DI 처리
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final UserRepository userRepository;

    // 코드 추가부분
    // 사용자 이름만으로 정보 조회
    public User readUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    /**
     * 회원 생성 서비스
     * @param dto
     */
    @Transactional // 트랜 잭션 처리 습관
    public void createUser(SignUpDTO dto) {
        // Http 응답으로 클라이언트에게 전달할 오류 메시지는 최소한으로 유지하고,
        // 보안 및 사용자 경험 측면에서 민감한 정보를 노출하지 않도록 합니다.
        int result = 0;
        if (!dto.getMFile().isEmpty()) {
            String[] fileNames =uploadFile(dto.getMFile());
            // dto 객체 상태 변경
            dto.setOriginFileName(fileNames[0]);
            dto.setUploadFileName(fileNames[1]);
        }
        try {
            // 코드 추가부분
            // 회원가입 요청자가 제출한 password 부분을 암호화 처리
            String hashPwd = passwordEncoder.encode(dto.getPassword());
            dto.setPassword(hashPwd);
            result = userRepository.insert(dto.toUser());
            // 여기서 예외 처리를 하면 상위 catch 블록에서 예외를 잡는다.
        } catch (DataAccessException e) {
            // DataAccessException는 Spring의 데이터 액세스 예외 클래스로,
            // 데이터베이스 연결이나 쿼리 실행과 관련된 문제를 처리합니다.
            throw new DataDeliveryException("잘못된 처리 입니다",HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // 그 외 예외 처리 - 페이지 이동 처리
            throw new RedirectException("알 수 없는 오류" , HttpStatus.SERVICE_UNAVAILABLE);
        }
        // 예외 클래스가 발생이 안되지만 프로세스 입장에서 예외 상황으로 바라 봄
        if (result != 1) {
            // 삽입된 행의 수가 1이 아닌 경우 예외 발생
            throw new DataDeliveryException("회원 가입 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//
//    public User readUser(SignInDTO dto) {
//        User user = null;
//        try {
//            user = userRepository.findByUsernameAndPassword(dto.getUsername(), dto.getPassword());
//            // 여기서 예외 처리를 하면 상위 catch 블록에서 예외를 잡는다.
//        } catch (DataAccessException e) {
//            // DataAccessException는 Spring의 데이터 액세스 예외 클래스로,
//            // 데이터베이스 연결이나 쿼리 실행과 관련된 문제를 처리합니다.
//            throw new DataDeliveryException("잘못된 처리 입니다",HttpStatus.INTERNAL_SERVER_ERROR);
//        } catch (Exception e) {
//            // 그 외 예외 처리 - 페이지 이동 처리
//            throw new RedirectException("알 수 없는 오류" , HttpStatus.SERVICE_UNAVAILABLE);
//        }
//        if(user == null) {
//            throw new DataDeliveryException("아이디 혹은 비번이 틀렸습니다.",
//                    HttpStatus.BAD_REQUEST);
//        }
//        return user;
//    }

    /**
     * 로그인 서비스
     * @param dto
     * @return userEntity or null
     */
    public User signIn(SignInDTO dto) {
        User userEntity = null;
        try {
            userEntity = userRepository.findByUsername(dto.getUsername());
        } catch (DataAccessException e) {
            throw new DataDeliveryException(Define.INVALID_INPUT,HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            throw new RedirectException(Define.UNKNOWN , HttpStatus.SERVICE_UNAVAILABLE);
        }

        if(userEntity == null) {
            throw new DataDeliveryException("아이디를 확인해주세요",
                    HttpStatus.BAD_REQUEST);
        }

        boolean isPwdMatched = passwordEncoder.matches(dto.getPassword(),
                userEntity.getPassword());
        if(isPwdMatched == false) {
            throw new DataDeliveryException("비밀번호가 잘못되었습니다.",
                    HttpStatus.BAD_REQUEST);
        }
        return userEntity;
    }


    // 1. 코드 추가
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     *
     * @param file
     * MultipartFile getOriginalFilename : 사용자가 작성한 파일 명
     * uploadFileName : 서버 컴퓨터에 저장 될 파일 명
     * @return index 0, 1
     */
    private String[] uploadFile(MultipartFile file) {
        if (file.getSize() > Define.MAX_FILE_SIZE) {
            throw new DataDeliveryException("파일 크키는 20MB 이상 클 수 없습니다.",HttpStatus.BAD_REQUEST);
        }


        // 코드 수정
        // getAbsolutePath() : 파일 시스템의 절대 경로를 나타냅니다.
        // (리눅스 또는 MacOS)
        String saveDirectory = new File(uploadDir).getAbsolutePath();
        File directory = new File(saveDirectory);
        // 폴더가 없다면 생성 처리
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 파일 이름 (중복 처리 예방)
        String uploadFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String uploadPath = saveDirectory + File.separator + uploadFileName;
        System.out.println("uploadPath : " + uploadPath);
        File destination = new File(uploadPath);
        System.out.println("destination : " + destination);

        try {
            file.transferTo(destination);
        }catch (IllegalStateException | IOException e) {
            throw new DataDeliveryException("파일 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new String[] {file.getOriginalFilename(), uploadFileName};
    }


}
