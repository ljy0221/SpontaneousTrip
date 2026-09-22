package com.ssafy.passproject.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

  private final JavaMailSender emailSender;

  @Value("${spring.mail.username}")
  private String from;

  public void sendTemporaryPassword(String to, String tempPassword) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(to);
    message.setSubject("[PASS PROJECT] 임시 비밀번호 발급 안내");
    message.setText("안녕하세요.\n\n" +
        "요청하신 임시 비밀번호는 다음과 같습니다.\n\n" +
        "임시 비밀번호: " + tempPassword + "\n\n" +
        "로그인 후 반드시 비밀번호를 변경해 주세요.\n\n" +
        "감사합니다.");

    try {
      emailSender.send(message);
      log.info("임시 비밀번호 이메일 전송 성공: {}", to);
    } catch (Exception e) {
      log.error("이메일 전송 실패: {}", e.getMessage());
      throw new RuntimeException("이메일 전송 중 오류가 발생했습니다.");
    }
  }
}
