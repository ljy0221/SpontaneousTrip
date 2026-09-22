package com.ssafy.passproject.domain.auth.service.impl;

import com.ssafy.passproject.domain.auth.dto.security.CustomUserDetails;
import com.ssafy.passproject.domain.user.entity.User;
import com.ssafy.passproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> user = userRepository.findByEmail(username);

        if(user.isPresent()){
            return new CustomUserDetails(user.get());
        }

        throw new UsernameNotFoundException("찾을 수 없는 유저 : " + username);
    }
}
