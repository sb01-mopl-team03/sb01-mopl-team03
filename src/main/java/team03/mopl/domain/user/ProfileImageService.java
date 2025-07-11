package team03.mopl.domain.user;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProfileImageService {

  public List<String> getProfileImages(){
    return List.of(
        "/profile/buzz.jpeg",
        "/profile/jessie.jpeg",
        "/profile/rex.png",
        "/profile/woody.jpeg"
    );
  }
}