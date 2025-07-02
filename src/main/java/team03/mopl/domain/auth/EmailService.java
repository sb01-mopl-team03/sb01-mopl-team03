package team03.mopl.domain.auth;

public interface EmailService  {
  void sendTempPassword(String to,String password);
}
