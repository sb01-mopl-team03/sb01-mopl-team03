package team03.mopl.domain.auth;

public record LoginResponse(
    String accessToken, boolean isTempPassword
) {
}
