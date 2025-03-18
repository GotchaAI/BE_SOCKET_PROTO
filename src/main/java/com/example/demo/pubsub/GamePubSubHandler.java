package com.example.demo.pubsub;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import static com.example.demo.config.WebSocketConstants.GAME_END_CHANNEL;
import static com.example.demo.config.WebSocketConstants.GAME_START_CHANNEL;

@Service
public class GamePubSubHandler extends PubSubHandler {

    public GamePubSubHandler(SimpMessagingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @Override
    protected void initHandlers() {
        handlers.put(GAME_START_CHANNEL, (channel, message) -> handleGameStart(message));
        handlers.put(GAME_END_CHANNEL, (channel, message) -> handleGameEnd(message));
    }

    // 게임 관련 처리 메소드
    private void handleGameStart(String message) {
        messagingTemplate.convertAndSend(GAME_START_CHANNEL, message);  // 게임 시작 메시지 발송
    }

    private void handleGameEnd(String message) {
        messagingTemplate.convertAndSend(GAME_END_CHANNEL, message);  // 게임 종료 메시지 발송
    }
}
