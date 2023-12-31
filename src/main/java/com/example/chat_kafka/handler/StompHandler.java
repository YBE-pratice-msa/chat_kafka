package com.example.chat_kafka.handler;

import com.example.chat_kafka.dto.ChatMessageDto;
import com.example.chat_kafka.repository.ChatRoomRepository;
import com.example.chat_kafka.service.ChatService;
import com.example.chat_kafka.type.MessageType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

//    private final JwtTokenProvider jwtTokenProvider;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;

    // websocket을 통해 들어온 요청이 처리 되기전 실행된다.
    /** JWT 토큰 검증하는 공간 -> 필요 없으니 일단 주석 */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        log.info("StompHandler : {}, command : {}", message.getPayload(), accessor.getCommand());

        if (StompCommand.CONNECT == accessor.getCommand()) {
            // websocket 연결요청
            /** JWT token 존재 x */

//            String jwtToken = accessor.getFirstNativeHeader("token");
//            log.info("CONNECT {}", jwtToken);
//            // Header의 jwt token 검증
//            jwtTokenProvider.validateToken(jwtToken);

        } else if (StompCommand.SEND == accessor.getCommand()) {

        } else if (StompCommand.SUBSCRIBE == accessor.getCommand()) { // 채팅룸 구독요청

            // header정보에서 구독 destination정보를 얻고, roomId를 추출한다.
            String roomId = chatService.getRoomId(
                    Optional.ofNullable((String) message.getHeaders().get("simpDestination")).orElse("InvalidRoomId")
            );

            // 채팅방에 들어온 클라이언트 sessionId를 roomId와 맵핑해 놓는다.(나중에 특정 세션이 어떤 채팅방에 들어가 있는지 알기 위함)
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            log.info("simpSessionId : {}", sessionId);
            chatRoomRepository.setUserEnterInfo(sessionId, roomId);

            // 채팅방의 인원수를 +1한다.
            chatRoomRepository.plusUserCount(roomId);


        } else if (StompCommand.DISCONNECT == accessor.getCommand()) { // Websocket 연결 종료

            // 연결이 종료된 클라이언트 sesssionId로 채팅방 id를 얻는다.
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            String roomId = chatRoomRepository.getUserEnterRoomId(sessionId);
            String name = chatRoomRepository.getUserInfoBySessionId(sessionId);

            // 채팅방의 인원수를 -1한다.
            chatRoomRepository.minusUserCount(roomId);

            chatService.sendChatMessage(
                    ChatMessageDto.builder().type(MessageType.QUIT).roomId(roomId).sender(name).build()
            );

            // 퇴장한 클라이언트의 roomId 맵핑 정보를 삭제한다.
            chatRoomRepository.removeUserEnterInfo(sessionId);
            chatRoomRepository.deleteUserInfo(sessionId);
            log.info("DISCONNECTED {}, {}", sessionId, roomId);
        }

        return message;
    }
}
