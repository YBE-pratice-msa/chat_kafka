package com.example.chat_kafka.util;

import com.example.chat_kafka.dto.ChatMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Utils() {
    }

    public static ChatMessageDto getObject(final String message) throws Exception {
        return objectMapper.readValue(message, ChatMessageDto.class);
    }

    public static String getString(final ChatMessageDto message) throws Exception {
        return objectMapper.writeValueAsString(message);
    }
}
