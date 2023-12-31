package com.example.chat_kafka.pubsub;

import com.example.chat_kafka.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaConsumer {
    private final SimpMessageSendingOperations messagingTemplate;

    @KafkaListener(
            topics = "${spring.kafka.template.default-topic}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void receiveMessage(ChatMessageDto message) {
        // 채팅방을 구독한 클라이언트에게 메시지 발송
        try {
            log.info("KafkaSubscriber publishMsg: {}", message);
            messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
        } catch (Exception e) {
            log.error("Exception {}", e);
        }
    }
}
