package team03.mopl.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import team03.mopl.domain.auth.ChangePasswordRequest;
import team03.mopl.domain.auth.LoginRequest;
import team03.mopl.domain.auth.LoginResponse;
import team03.mopl.domain.auth.TempPasswordRequest;
import team03.mopl.jwt.CustomUserDetails;

@Tag(name = "Auth API", description = "인증 및 권한 관련 API")
@RequestMapping("/api/auth")
public interface AuthApi {

  @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 Access/Refresh 토큰을 발급받습니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공",
          content = @Content(schema = @Schema(implementation = LoginResponse.class))),
      @ApiResponse(responseCode = "401", description = "잘못된 로그인 정보"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping("/login")
  ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response);

  @Operation(summary = "로그아웃", description = "토큰 기반 세션을 만료시킵니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
      @ApiResponse(responseCode = "400", description = "쿠키에 Refresh 토큰 없음")
  })
  @PostMapping("/logout")
  ResponseEntity<?> logout(@CookieValue(value = "refresh", required = false) String refreshToken);

  @Operation(summary = "Refresh 토큰 재발급", description = "Refresh 토큰을 재발급합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "재발급 성공"),
      @ApiResponse(responseCode = "401", description = "세션 없음 또는 만료됨")
  })
  @PostMapping("/refresh")
  ResponseEntity<?> refresh(@CookieValue(value = "refresh", required = false) String refreshToken, HttpServletResponse response);

  @Operation(summary = "Access 토큰 재발급", description = "Refresh 토큰으로 Access 토큰만 재발급합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "재발급 성공"),
      @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Refresh 토큰")
  })
  @PostMapping("/me")
  ResponseEntity<?> reissueAccessToken(@CookieValue(value = "refresh", required = false) String refreshToken);

  @Operation(summary = "임시 비밀번호 발급", description = "이메일을 기반으로 임시 비밀번호를 이메일로 전송합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "임시 비밀번호 발급 성공"),
      @ApiResponse(responseCode = "500", description = "발급 실패")
  })
  @PostMapping("/temp-password")
  ResponseEntity<?> resetPassword(@RequestBody TempPasswordRequest request);

  @Operation(summary = "비밀번호 변경", description = "로그인된 사용자가 비밀번호를 새롭게 설정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping("/change-password")
  ResponseEntity<?> changePassword(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody ChangePasswordRequest request);
}
