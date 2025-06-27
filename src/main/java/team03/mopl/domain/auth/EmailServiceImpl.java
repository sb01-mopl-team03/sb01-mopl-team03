package team03.mopl.domain.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;

  @Override
  public void sendTempPassword(String to, String password) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject("[모두의 플리] 임시 비밀번호 안내");
    message.setBcc("""
        요청하신 임시 비밀번호는 아래와 같습니다.
        
        %s
        
        30분 이내로 로그인 후 반드시 변경해주세요.
        """.formatted(password));

    mailSender.send(message);
  }
}
