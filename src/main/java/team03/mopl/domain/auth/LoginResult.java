package team03.mopl.domain.auth;

public record LoginResult(
    String accessToken,
    String refreshToken,
    boolean isTempPassword
) {

}
