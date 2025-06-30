package team03.mopl.domain.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.user.UserResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  private final AdminService adminService;

  @PutMapping("/role")
  public ResponseEntity<UserResponse> updateUserRole(@RequestBody RoleUpdateRequest request){
    return ResponseEntity.ok(adminService.changeRole(request.userId(),request.newRole()));
  }

  @PutMapping("/lock")
  public ResponseEntity<UserResponse> lockUser(@RequestBody LockRequest request) {
    return ResponseEntity.ok(adminService.lockUser(request.userId()));
  }

  @PutMapping("/unlock")
  public ResponseEntity<UserResponse> unlockUser(@RequestBody LockRequest request) {
    return ResponseEntity.ok(adminService.unlockUser(request.userId()));
  }
}
