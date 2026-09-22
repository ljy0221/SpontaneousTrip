package com.ssafy.passproject.domain.user.repository;

import com.ssafy.passproject.domain.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface UserRepository {
    int registUser(User user);

    Optional<User> findByEmail(String email);

    int updateUser(User user);

    int deleteUser(int userId);

    int updateLastLogin(String email);
}
