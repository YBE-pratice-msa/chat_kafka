package com.example.chat_kafka.dto;

import com.example.chat_kafka.entity.ChatMessage;
import com.example.chat_kafka.type.MessageType;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto  {
    // 메시지  타입 : 입장, 채팅, 퇴장

    private MessageType type; // 메시지 타입
    private String roomId; // 방 번호
    private String sender; // 채팅을 보낸 사람
    private String message; // 메시지
    private long userCount; // 채팅방 인원 수

    public static ChatMessageDto fromEntity(ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .type(chatMessage.getType())
                .sender(chatMessage.getSender())
                .roomId(chatMessage.getRoomId())
                .message(chatMessage.getMessage())
                .build();
    }

}
