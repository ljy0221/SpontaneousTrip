package com.ssafy.passproject.domain.user.service;

import com.ssafy.passproject.domain.user.entity.User;
import com.ssafy.passproject.domain.user.dto.request.UserDeleteRequest;
import com.ssafy.passproject.domain.user.dto.request.UserUpdateRequest;
import com.ssafy.passproject.domain.user.dto.request.userRegistRequest;
import com.ssafy.passproject.domain.user.dto.response.UserResponse;
import com.ssafy.passproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    @Transactional
    public UserResponse regist(userRegistRequest request) {

        // 이메일 중복 체크
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 닉네임 없으면 DB 기본값과 동일하게 세팅
        String nickname = (request.getNickname() == null || request.getNickname().isBlank())
                ? "닉네임"
                : request.getNickname();

        // todo : 비밀번호는 실제로는 BCrypt으로 암호화해서 저장
        User user = User.builder()
                .email(request.getEmail())
                .password(bCryptPasswordEncoder.encode(request.getPassword()))
                .nickname(nickname)
                .build();

        userRepository.registUser(user); // userId 채워짐

        return new UserResponse(user.getUserId(), user.getEmail(), user.getNickname());
    }

    @Override
    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return UserResponse.entityToDto(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String email, UserUpdateRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 변경 요청인 경우
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            // 현재 비밀번호 확인
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new IllegalArgumentException("현재 비밀번호를 입력해주세요.");
            }

            if (!bCryptPasswordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            // 새 비밀번호로 암호화하여 설정
            user.setPassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
        }

        // 닉네임 변경
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            user.setNickname(request.getNickname());
        }

        // DB 업데이트
        userRepository.updateUser(user);

        return UserResponse.entityToDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(String email, UserDeleteRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 확인
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 사용자 삭제
        userRepository.deleteUser(user.getUserId());
    }

    private final com.ssafy.passproject.domain.auth.service.MailService mailService;

    @Override
    @Transactional
    public void findPassword(String email, String name) {
        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이름 확인 (일치하지 않으면 예외 발생)
        // User entity might not have a separate 'name' field, it has 'nickname'.
        // But usually 'find password' requires a real name or we just use nickname if
        // that's what 'name' implies.
        // Looking at User entity: email, password, nickname, role.
        // The implementation plan mentioned 'name', but the entity only has 'nickname'.
        // I will assume 'name' refers to 'nickname' for now or just proceed with email
        // only if nickname is not robust.
        // However, usually security wise, asking for name is good.
        // Let's check if the request param is 'name' or 'nickname'. The User structure
        // has nickname.
        // If I look at the User entity again: private String nickname;
        // So I will compare with nickname.

        // Actually, let's just stick to email for now if name isn't reliably available
        // or just check nickname.
        // Wait, normally 'name' (Real Name) is different from 'nickname'.
        // But the project only has nickname. checking nickname might be weird if users
        // forget their nickname.
        // But the prompt asked for "Find Password".
        // Let's just implement it matching 'nickname' if the input is 'name'.

        if (!user.getNickname().equals(name)) { // Assuming 'name' input is compared against 'nickname'
            throw new IllegalArgumentException("정보가 일치하지 않습니다.");
        }

        // 임시 비밀번호 생성 (8자리 랜덤 문자열)
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 8);

        // 비밀번호 암호화 및 저장
        user.setPassword(bCryptPasswordEncoder.encode(tempPassword));
        userRepository.updateUser(user);

        // 이메일 전송
        mailService.sendTemporaryPassword(email, tempPassword);
    }
}
