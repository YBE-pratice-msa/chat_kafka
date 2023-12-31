package com.example.chat_kafka.repository;

import com.example.chat_kafka.dto.ChatRoomDto;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.C;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ChatRoomRepository {
    // Redis CacheKeys
    private static final String CHAT_ROOMS = "CHAT_ROOM_KAFKA";
    public static final String USER_COUNT = "USER_COUNT"; // 채팅룸에 입장한 클라이언트수 저장
    public static final String ENTER_INFO = "ENTER_INFO"; // 채팅룸에 입장한 클라이언트의 sessionId와 채팅룸 id를 맵핑한 정보 저장

    // HashOperations<String, String, ChatRoom> : Redis의 해시 데이터 구조를 다룸.
    // String 타입의 key, String 타입의 필드, chatRoom 객체의 값으로 구성된 해시를 다룬다.

    @Resource(name = "redisTemplate") //redisTemplate bean 주입.
    private HashOperations<String, String, ChatRoomDto> opsHashChatRoom;

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOpsEnterInfo;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOps;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> userInfoOps;

    // 모든 채팅방 조회
    public List<ChatRoomDto> findAllRoom() {
        List<ChatRoomDto> chatRoomDtos = opsHashChatRoom.values(CHAT_ROOMS);
        if (chatRoomDtos == null || chatRoomDtos.size() == 0) {
            chatRoomDtos = new ArrayList<>();
        }
        return chatRoomDtos;
    }

    // 특정 채팅방 조회
    public ChatRoomDto findRoomById(String id) {
        return opsHashChatRoom.get(CHAT_ROOMS, id);
    }

    // 채팅방 생성 : 서버간 채팅방 공유를 위해 redis hash에 저장한다.
    public ChatRoomDto createChatRoom(String name) {
        ChatRoomDto chatRoomDto = ChatRoomDto.create(name);
        opsHashChatRoom.put(CHAT_ROOMS, chatRoomDto.getRoomId(), chatRoomDto);
        return chatRoomDto;
    }

    // 세션 id와 유저 정보 저장 .
    public void setUserInfoBySessionId(String sessionId, String name) {
        if (userInfoOps.get(sessionId) == null) userInfoOps.append(sessionId, name);
    }

    // 세션 id와 유저 정보 가져오기.
    public String getUserInfoBySessionId(String sessionId) {
        return userInfoOps.get(sessionId);
    }

    // 퇴장 시 세션 id 값 삭제
    public void deleteUserInfo(String sessionId) {
        userInfoOps.getAndDelete(sessionId);
    }

    // 유저가 입장한 채팅방 ID와 유저 세션 ID 맵핑 정보 저장
    public void setUserEnterInfo(String sessionId, String roomId) {
        hashOpsEnterInfo.put(ENTER_INFO, sessionId, roomId);
    }

    // 유저 세션으로 입장해 있는 채팅방 ID 조회
    public String getUserEnterRoomId(String sessionId) {
        return hashOpsEnterInfo.get(ENTER_INFO, sessionId);
    }

    // 유저 세션정보와 맵핑된 채팅방ID 삭제
    public void removeUserEnterInfo(String sessionId) {
        hashOpsEnterInfo.delete(ENTER_INFO, sessionId);
    }

    // 채팅방 유저수 조회
    public long getUserCount(String roomId) {
        return Long.valueOf(Optional.ofNullable(valueOps.get(USER_COUNT + "_" + roomId)).orElse("0"));
    }

    // 채팅방에 입장한 유저수 +1
    public long plusUserCount(String roomId) {
        return Optional.ofNullable(valueOps.increment(USER_COUNT + "_" + roomId)).orElse(0L);
    }

    // 채팅방에 입장한 유저수 -1
    public long minusUserCount(String roomId) {
        return Optional.ofNullable(valueOps.decrement(USER_COUNT + "_" + roomId)).filter(count -> count > 0).orElse(0L);
    }


}
