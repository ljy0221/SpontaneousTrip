package com.ssafy.passproject.domain.user.service;

import com.ssafy.passproject.domain.user.dto.request.UserDeleteRequest;
import com.ssafy.passproject.domain.user.dto.request.UserUpdateRequest;
import com.ssafy.passproject.domain.user.dto.request.userRegistRequest;
import com.ssafy.passproject.domain.user.dto.response.UserResponse;

public interface UserService {
    UserResponse regist(userRegistRequest request);

    UserResponse findByEmail(String email);

    UserResponse updateUser(String email, UserUpdateRequest request);

    void deleteUser(String email, UserDeleteRequest request);

    void findPassword(String email, String name);
}
