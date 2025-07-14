package team03.mopl.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleException(Exception e, HttpServletRequest request) {
    log.error("Exception occurred: {}", e.getMessage(), e);

    // SSE 요청인지 확인
    String acceptHeader = request.getHeader("Accept");
    if (acceptHeader != null && acceptHeader.contains("text/event-stream")) {
      log.warn("SSE 요청에서 예외 발생: {}", e.getMessage());
      // SSE 요청에서는 JSON 대신 텍스트 응답
      return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .contentType(MediaType.TEXT_PLAIN)
          .body("SSE connection error: " + e.getMessage());
    }

    // 일반 요청은 기존처럼 JSON 응답
    ErrorResponse errorResponse = new ErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR.value());
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(errorResponse);
  }

  @ExceptionHandler(MoplException.class)
  public ResponseEntity<?> handleMoplException(MoplException exception, HttpServletRequest request) {
    log.error("MoplException occurred: {}", exception.getMessage(), exception);

    // SSE 요청인지 확인
    String acceptHeader = request.getHeader("Accept");
    if (acceptHeader != null && acceptHeader.contains("text/event-stream")) {
      log.warn("SSE 요청에서 MoplException 발생: {}", exception.getMessage());
      return ResponseEntity
          .status(HttpStatus.valueOf(exception.getMessage()))
          .contentType(MediaType.TEXT_PLAIN)
          .body("SSE error: " + exception.getMessage());
    }

    // 일반 요청은 기존처럼 JSON 응답
    ErrorResponse response = new ErrorResponse(exception);
    return ResponseEntity
        .status(response.getStatus())
        .body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
    log.error("IllegalArgumentException occurred: {}", e.getMessage(), e);

    // SSE 요청인지 확인
    String acceptHeader = request.getHeader("Accept");
    if (acceptHeader != null && acceptHeader.contains("text/event-stream")) {
      log.warn("SSE 요청에서 IllegalArgumentException 발생: {}", e.getMessage());
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .contentType(MediaType.TEXT_PLAIN)
          .body("SSE bad request: " + e.getMessage());
    }

    // 일반 요청은 기존처럼 JSON 응답
    ErrorResponse errorResponse = new ErrorResponse(e, HttpStatus.BAD_REQUEST.value());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * @Valid 검증 실패시 MethodArgumentNotValidException 예외 발생
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
    log.error("MethodArgumentNotValidException occurred: {}", e.getMessage(), e);

    // SSE 요청인지 확인
    String acceptHeader = request.getHeader("Accept");
    if (acceptHeader != null && acceptHeader.contains("text/event-stream")) {
      log.warn("SSE 요청에서 Validation 예외 발생: {}", e.getMessage());
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .contentType(MediaType.TEXT_PLAIN)
          .body("SSE validation error: " + e.getMessage());
    }

    // 일반 요청은 기존처럼 JSON 응답
    ErrorResponse errorResponse = new ErrorResponse(e, HttpStatus.BAD_REQUEST.value());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }
}