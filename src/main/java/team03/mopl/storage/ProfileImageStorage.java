package team03.mopl.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageStorage {
  String upload(MultipartFile file);

  void delete(String profileImage);
}
