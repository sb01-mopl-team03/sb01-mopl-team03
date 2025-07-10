package team03.mopl.storage;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@ConditionalOnProperty(name = "mopl.storage.type",havingValue = "s3")
@RequiredArgsConstructor
public class S3ProfileStorage implements ProfileImageStorage{

  @Value("${mopl.storage.s3.bucket}")
  private String bucket;

  private final S3Client s3Client;

  @Override
  public String upload(MultipartFile file) {
    String fileName = UUID.randomUUID()+"_"+file.getOriginalFilename();
    try{
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(fileName)
              .contentType(file.getContentType())
              .acl(ObjectCannedACL.PUBLIC_READ)
              .build(),
          software.amazon.awssdk.core.sync.RequestBody.fromInputStream(file.getInputStream(),
              file.getSize())
      );
    } catch (IOException e) {
      throw new RuntimeException("s3 업로드 실패",e);
    }
    return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(fileName)).toExternalForm();
  }

  @Override
  public void delete(String profileImage) {
    try{
      String key = URI.create(profileImage).getPath().substring(1);
      s3Client.deleteObject(builder -> builder.bucket(bucket).key(key));
    }catch (Exception e) {
      throw new RuntimeException("이미지 삭제 실패",e);
    }
  }
}
