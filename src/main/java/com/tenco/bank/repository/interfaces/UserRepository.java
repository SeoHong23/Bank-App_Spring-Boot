package com.tenco.bank.repository.interfaces;

import com.tenco.bank.repository.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// 인터페이스 만들고 + XML 파일 정의한다.


@Mapper // 반드시 선언 해 주어야 한다!
public interface UserRepository {
    public int insert(User user);
    public int updateById(User user);
    public int deleteById(Integer id);
    public int findById(Integer id);
    public List<User> findAll();
    public User findByUsername(String username);
    public  User
    findByUsernameAndPassword(
            @Param("username")String username,
            @Param("password")String password);

}