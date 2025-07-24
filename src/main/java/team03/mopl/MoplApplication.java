package team03.mopl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MoplApplication {

  public static void main(String[] args) {
    SpringApplication.run(MoplApplication.class, args);
  }

}
