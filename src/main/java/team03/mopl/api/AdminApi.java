package team03.mopl.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import team03.mopl.domain.admin.LockRequest;
import team03.mopl.domain.admin.RoleUpdateRequest;
import team03.mopl.domain.user.UserResponse;

@Tag(name = "Admin API", description = "관리자용 API")
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public interface AdminApi {

  @Operation(summary = "사용자 권한 변경", description = "관리자가 특정 사용자의 권한을 변경합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "권한 변경 성공",
      content = @Content(schema = @Schema(implementation = UserResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PutMapping("/role")
  ResponseEntity<UserResponse> updateUserRole(@RequestBody RoleUpdateRequest request);

  @Operation(summary = "사용자 계정 잠금", description = "관리자가 사용자 계정을 잠급니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "잠금 성공",
          content = @Content(schema = @Schema(implementation = UserResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PutMapping("/lock")
  ResponseEntity<UserResponse> lockUser(@RequestBody LockRequest request);

  @Operation(summary = "사용자 계정 잠금 해제", description = "관리자가 사용자 계정의 잠금을 해제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "잠금 해제 성공",
          content = @Content(schema = @Schema(implementation = UserResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PutMapping("/unlock")
  ResponseEntity<UserResponse> unlockUser(@RequestBody LockRequest request);
}
