package com.example.demo.pubsub;

import com.example.demo.dto.GameReadyStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import static com.example.demo.config.WebSocketConstants.*;

@Service
public class GamePubSubHandler extends PubSubHandler {

    public GamePubSubHandler(SimpMessagingTemplate messagingTemplate) {
        super(messagingTemplate);
    }

    @Override
    protected void initHandlers() {
        handlers.put(GAME_READY_CHANNEL, this::handleGameReady);
        handlers.put(GAME_START_CHANNEL, this::handleGameStart);
        handlers.put(GAME_END_CHANNEL, this::handleGameEnd);
        handlers.put(GAME_INFO_CHANNEL, this::handleGameInfo);
    }

    private void handleGameReady(String channel, String message) {
        GameReadyStatus gameReadyStatus = convertMessageToDto(message, GameReadyStatus.class);
        messagingTemplate.convertAndSend(channel, gameReadyStatus);
    }

    private void handleGameStart(String channel, String message) {
        messagingTemplate.convertAndSend(channel, message);
    }

    private void handleGameEnd(String channel, String message) {
        messagingTemplate.convertAndSend(channel, message);
    }

    private void handleGameInfo(String channel, String message) {

    }

}
