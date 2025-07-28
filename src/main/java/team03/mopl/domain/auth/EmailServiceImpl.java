package team03.mopl.domain.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;

  @Override
  public void sendTempPassword(String to, String password) {
    log.info("sentTempPassword - 메일 발송");
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject("[모두의 플리] 임시 비밀번호 안내");
    message.setText("""
        요청하신 임시 비밀번호는 아래와 같습니다.
        
        %s
        
        30분 이내로 로그인 후 반드시 변경해주세요.
        """.formatted(password));
    log.info("sentTempPassword - 메일 발송 완료");
    mailSender.send(message);
  }
}
