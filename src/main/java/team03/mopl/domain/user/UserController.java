package team03.mopl.domain.user;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.review.dto.ReviewResponse;
import team03.mopl.domain.review.service.ReviewService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final ProfileImageService profileImageService;
  private final ReviewService reviewService;

  @PostMapping
  public ResponseEntity<UserResponse> create(@RequestBody UserCreateRequest request) {
    return ResponseEntity.ok(userService.create(request));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserResponse> find(@PathVariable UUID userId) {
    return ResponseEntity.ok(userService.find(userId));
  }

  @PutMapping("/{userId}")
  public ResponseEntity<UserResponse> update(@PathVariable UUID userId, @RequestBody UserUpdateRequest request) {
    return ResponseEntity.ok(userService.update(userId,request));
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(@PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<UserResponse>> findAll() {
    return ResponseEntity.ok(userService.findAll());
  }

  @GetMapping("/profiles")
  public ResponseEntity<List<String>> getProfileImages() {
    return ResponseEntity.ok(profileImageService.getProfileImages());
  }

  @GetMapping("/{userId}/reviews")
  public ResponseEntity<List<ReviewResponse>> getAllByUser(@PathVariable UUID userId) {
    return ResponseEntity.ok(reviewService.getAllByUser(userId));
  }

}
