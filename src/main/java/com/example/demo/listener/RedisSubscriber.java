package com.example.demo.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

// Controller -> Redis로 받은 메시지를 다른 사용자들에게(WebSocket으로) 전달
@Service
public class RedisSubscriber implements MessageListener {

    private final Map<String, Consumer<String>> handlers = new HashMap<>();

    public RedisSubscriber() {
        handlers.put("chat:all", this::handleAllChat);
        handlers.put("chat:private:", this::handlePrivateChat);
        handlers.put("chat:room:", this::handleRoomChat);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern);
        String msg = message.toString();

        // 매칭되는 핸들러가 없으면 기본 핸들러 실행
        handlers.entrySet().stream()
                .filter(entry -> channel.startsWith(entry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(this::handleUnknownChannel) // 기본 핸들러
                .accept(msg);
    }

    private void handleAllChat(String message) {
        System.out.println("📢 [전체 채팅 로직 수행]");
    }

    private void handlePrivateChat(String message) {
        System.out.println("📩 [귓속말 로직 수행]");
    }

    private void handleRoomChat(String message) {
        System.out.println("🏠 [대기방 채팅 로직 수행]");
    }

    private void handleUnknownChannel(String message) {
        System.out.println("❌ [알 수 없는 채널] 처리할 수 없는 메시지: " + message);
    }
}
