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
        //추가 기능 생길 시 handlers.put()만 하면, 해당 주소로 접근시 매핑된 함수 자동 호출 됨.
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern);
        String msg = message.toString();

        // 핸들러 매칭
        handlers.entrySet().stream()
                .filter(entry -> channel.startsWith(entry.getKey()))
                .findFirst()
                .ifPresent(entry -> entry.getValue().accept(msg));
    }

    private void handleAllChat(String message) {
        System.out.println("📢 [전체 채팅] " + message);
    }

    private void handlePrivateChat(String message) {
        System.out.println("📩 [귓속말] " + message);
    }

    private void handleRoomChat(String message) {
        System.out.println("🏠 [대기방 채팅] " + message);
    }
}