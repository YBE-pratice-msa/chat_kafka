package com.example.chat_kafka.pubsub;

import com.example.chat_kafka.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class KafkaProducer {
    private final KafkaTemplate<String, ChatMessageDto> kafkaTemplate;

    // 메시지를 지정한 Kafka 토픽으로 전송.
    public void send(String topic, ChatMessageDto message) {
        log.info("KafkaPublisher topic : {}, message : {}", topic, message);
        kafkaTemplate.send(topic, message);
    }
}
