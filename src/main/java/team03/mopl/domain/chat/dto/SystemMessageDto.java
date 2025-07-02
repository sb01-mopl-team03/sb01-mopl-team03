package team03.mopl.domain.chat.dto;

//소켓 연결 및 채팅방 입장 성공 이벤트를 전달하기 위한 메세지 Dto
public record SystemMessageDto(
    //입장 시 알림 이외의 시스템 메시지 추가되면 Enum으로 리팩터링 필요
    String type,
    String message
) {

}
