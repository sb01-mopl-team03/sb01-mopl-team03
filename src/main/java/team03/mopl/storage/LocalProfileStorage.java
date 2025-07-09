package team03.mopl.storage;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@ConditionalOnProperty(name = "mopl.storage.type",havingValue = "local",matchIfMissing = true)
@RequiredArgsConstructor
public class LocalProfileStorage implements ProfileImageStorage{

  @Value("${mopl.storage.local.root-path}")
  private String rootPath;

  private Path reslovedRootPath;

  @PostConstruct
  public void init() throws IOException {
    reslovedRootPath = Paths.get(rootPath).toAbsolutePath().normalize();
    Files.createDirectories(reslovedRootPath);
  }

  @Override
  public String upload(MultipartFile file) {
    try{
      String fileName = UUID.randomUUID()+"_"+file.getOriginalFilename();
      Path targetPath = reslovedRootPath.resolve(fileName);
      file.transferTo(targetPath.toFile());
      return "/local/profile/" + fileName;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("local 이미지 저장 실패",e);
    }
  }

  @Override
  public void delete(String profileImage) {
    try{
      String fileName = Paths.get(profileImage).getFileName().toString();
      Path path = Paths.get(rootPath).resolve(fileName);
      Files.deleteIfExists(path);
    } catch (IOException e) {
      throw new RuntimeException("이미지 삭제 실패",e);
    }
  }
}
