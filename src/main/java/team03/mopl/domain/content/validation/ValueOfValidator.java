package team03.mopl.domain.content.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// ConstraintValidator<A, T> A : 어노테이션 타입, T : 검증 대상 데이터 타입
public class ValueOfValidator implements ConstraintValidator<AllowedValues, String> {

  private Set<String> acceptedValues;

  @Override
  public void initialize(AllowedValues constraintAnnotation) {
    // AllowedValues 어노테이션에 지정된 enum 클래스에서 모든 enum 상수들을 가져온다.
    acceptedValues = Stream.of(constraintAnnotation.enumClass().getEnumConstants())
        // enum 상수 → String 문자열 변환
        .map(Enum::name)
        // 문자열로 변한 enum 이름을 Set에 수집
        .collect(Collectors.toSet());

  }

  @Override
  public boolean isValid(String s, ConstraintValidatorContext context) {
    // null, Blank 값은 유효성 검사 통과 (필요시 @NotNull 로 따로 검증)
    if ( s == null || s.isBlank() ){
      return true;
    }

    // acceptedValues 내에 입력 데이터 존재 여부 확인
    return acceptedValues.contains(s.toUpperCase());
  }
}
