package com.example.demo.controller;

import com.example.demo.dto.ChatMessageReq;
import com.example.demo.dto.GameEventReq;
import com.example.demo.dto.GameReadyStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;

import static com.example.demo.config.WebSocketConstants.*;


//WebSocket으로 들어온 메시지를 Redis에 발행
@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final RedisTemplate<String, Object> pubSubHandler;

    // 1. 전체 채팅방 메시지 전송
    @MessageMapping("/chat/all")
    public void sendMessageToAll(@Payload ChatMessageReq message) {
        pubSubHandler.convertAndSend(CHAT_ALL_CHANNEL, message);
    }

    // 2. 귓속말 전송
    @MessageMapping("/chat/private")
    public void sendPrivateMessage(@Payload ChatMessageReq message) {
        String targetChannel = CHAT_PRIVATE_CHANNEL + message.nickName();
        pubSubHandler.convertAndSend(targetChannel, message);
    }

    // 3. 대기방 메시지 전송
    @MessageMapping("/chat/room/{roomId}")
    public void sendRoomMessage(@DestinationVariable String roomId, @Payload ChatMessageReq message) {
        pubSubHandler.convertAndSend(CHAT_ROOM_CHANNEL + roomId, message);
    }

    // 4. 대기방 레디 상태 업데이트
    @MessageMapping("/game/ready/{roomId}")
    public void sendReadyStatus(@DestinationVariable String roomId, @Payload GameReadyStatus readyStatus) {
        pubSubHandler.convertAndSend(GAME_READY_CHANNEL + roomId, readyStatus);
    }

    //5. 게임 시작 전 게임 정보(제시어) 전달
    @MessageMapping("/game/info/{roomId}")
    public void sendGameInfoBeforeStart(@DestinationVariable String roomId) {

    }

    // 6. 게임 시작
    @MessageMapping("/game/start/{roomId}")
    public void startGame(@DestinationVariable String roomId) {
        long endTime = System.currentTimeMillis() + 30000; // 30초 후 종료
        GameEventReq startMessage = new GameEventReq(roomId, endTime);
        pubSubHandler.convertAndSend(GAME_START_CHANNEL + roomId, startMessage);
    }

    // 7. 게임 종료
    @MessageMapping("/game/end/{roomId}")
    public void endGame(@DestinationVariable String roomId) {
        pubSubHandler.convertAndSend(GAME_END_CHANNEL + roomId, GAME_END_MESSAGE);
    }

}
