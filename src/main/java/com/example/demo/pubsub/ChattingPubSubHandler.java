package com.example.demo.pubsub;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import static com.example.demo.config.WebSocketConstants.*;

@Service
public class ChattingPubSubHandler extends PubSubHandler {

    public ChattingPubSubHandler(SimpMessagingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @Override
    protected void initHandlers() {
        handlers.put(CHAT_ALL_CHANNEL, (channel, message) -> handleAllChat(message));
        handlers.put(CHAT_PRIVATE_CHANNEL, (channel, message) -> handlePrivateChat(channel, message));
        handlers.put(CHAT_ROOM_CHANNEL, (channel, message) -> handleRoomChat(channel, message));
    }

    // 채팅 처리 메소드
    private void handleAllChat(String message) {
        messagingTemplate.convertAndSend(CHAT_ALL_CHANNEL, message);  // 전체 채팅방 메시지 처리
    }

    private void handlePrivateChat(String channel, String message) {
        String nickName = channel.replace(CHAT_PRIVATE_CHANNEL, "");
        messagingTemplate.convertAndSend(CHAT_PRIVATE_CHANNEL + nickName, message);  // 귓속말 처리
    }

    private void handleRoomChat(String channel, String message) {
        String roomId = channel.replace(CHAT_ROOM_CHANNEL, "");
        messagingTemplate.convertAndSend(CHAT_ROOM_CHANNEL + roomId, message);  // 대기방 채팅 처리
    }
}
