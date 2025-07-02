package team03.mopl.domain.oauth2;

public record GoogleUserInfo(
    String email,
    String name,
    String picture
) {
}
