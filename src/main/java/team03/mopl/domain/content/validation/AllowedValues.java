package team03.mopl.domain.content.validation;

import com.nimbusds.jose.Payload;
import jakarta.validation.Constraint;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 적용 위치 : 필드, 파라미터
@Target({ElementType.FIELD, ElementType.PARAMETER})
// 어노테이션 유지 시간 : 실행 중에도 리플렉션으로 읽을 수 있다 → 검증에 필요
@Retention(RetentionPolicy.RUNTIME)
// JavaDoc에 포함시키는 어노테이션
@Documented
// 검증용 제약조건 : 검증 로직이 들어있는 클래스 지정
@Constraint(validatedBy = ValueOfValidator.class)
public @interface AllowedValues {
  
  // Enum 클래스를 상속하는 클래스를 지정받는 속성
  Class<? extends Enum<?>> enumClass();

  // 검증 실패 시 표시할 메시지를 설정하는 속성
  String message() default "유효하지 않는 값입니다.";
  // 검증이 속하는 validation 그룹 속성 : 현재 아무 그룹에도 속하지 않는다
  Class<?>[] groups() default {};
  // 메타데이터 추가 가능 속성
  Class<? extends Payload>[] payload() default {};
  
  // 대소문자 무시 여부 : 대소문자 무시
  boolean ignoreCase() default true;
}
