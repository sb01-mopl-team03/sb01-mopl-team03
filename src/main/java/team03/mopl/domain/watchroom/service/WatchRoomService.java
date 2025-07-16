package team03.mopl.domain.watchroom.service;

import java.util.UUID;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.watchroom.dto.WatchRoomCreateRequest;
import team03.mopl.domain.watchroom.dto.WatchRoomDto;
import team03.mopl.domain.watchroom.dto.participant.ParticipantsInfoDto;
import team03.mopl.domain.watchroom.dto.video.VideoControlRequest;
import team03.mopl.domain.watchroom.dto.video.VideoSyncDto;
import team03.mopl.domain.watchroom.dto.WatchRoomInfoDto;
import team03.mopl.domain.watchroom.dto.WatchRoomSearchDto;

public interface WatchRoomService {

  //채팅방 생성
  WatchRoomDto create(WatchRoomCreateRequest request);

  //채팅방 전체 조회(페이지네이션)
  CursorPageResponseDto<WatchRoomDto> getAll(WatchRoomSearchDto request);

  //채팅방 단일 조회
  WatchRoomDto getById(UUID id);

  //채팅방 참여 후 상세정보 반환
  WatchRoomInfoDto joinWatchRoomAndGetInfo(UUID chatRoomId, String username);

  //시청방 비디오 제어
  VideoSyncDto updateVideoStatus(UUID roomId, VideoControlRequest request, String username);

  //시청방 유저 목록 조회
  ParticipantsInfoDto getParticipants(UUID roomId);

  //시청방 상세정보 조회
  WatchRoomInfoDto getWatchRoomInfo(UUID roomId);

  //시청방 나가기
  void leave(UUID roomId, String username);
}
