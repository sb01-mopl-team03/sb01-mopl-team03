package team03.mopl.domain.user;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.playlist.dto.PlaylistDto;
import team03.mopl.domain.playlist.service.PlaylistService;
import team03.mopl.domain.review.dto.ReviewDto;
import team03.mopl.domain.review.service.ReviewService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final ProfileImageService profileImageService;
  private final ReviewService reviewService;
  private final PlaylistService playlistService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserResponse> create(@ModelAttribute UserCreateRequest request) {
    return ResponseEntity.ok(userService.create(request));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserResponse> find(@PathVariable UUID userId) {
    return ResponseEntity.ok(userService.find(userId));
  }

  @PutMapping("/{userId}")
  public ResponseEntity<UserResponse> update(@PathVariable UUID userId,
      @RequestPart UserUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    return ResponseEntity.ok(userService.update(userId,request,profile));
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
  public ResponseEntity<List<ReviewResponse>> getAllReviewByUser(@PathVariable UUID userId) {
    return ResponseEntity.ok(reviewService.getAllByUser(userId));
  }

  @GetMapping("/{userId}/playlists")
  public ResponseEntity<List<PlaylistDto>> getAllPlaylistByUser(@PathVariable UUID userId) {
    return ResponseEntity.ok(playlistService.getAllByUser(userId));
  }

}
