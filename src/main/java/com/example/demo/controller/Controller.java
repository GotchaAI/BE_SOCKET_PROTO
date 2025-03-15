package com.example.demo.controller;

import com.example.demo.dto.ChatMessageReq;
import com.example.demo.dto.GameEventReq;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.redis.core.RedisTemplate;

//WebSocket으로 들어온 메시지를 Redis에 발행
@RestController
public class Controller {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public Controller(RedisTemplate<String, Object> redisTemplate, SimpMessagingTemplate messagingTemplate) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    // 1. 전체 채팅방 메시지 전송
    @MessageMapping("/chat/all")
    public void sendMessageToAll(@Payload ChatMessageReq message) {
        redisTemplate.convertAndSend("chat:all", message);
    }

    // 2. 귓속말 전송
    @MessageMapping("/chat/private")
    public void sendPrivateMessage(@Payload  ChatMessageReq message) {
        String targetChannel = "chat:private:" + message.nickName();
        redisTemplate.convertAndSend(targetChannel, message);
    }

    // 3. 대기방 메시지 전송
    @MessageMapping("/chat/room/{roomId}")
    public void sendRoomMessage(@DestinationVariable String roomId, @Payload ChatMessageReq message) {
        redisTemplate.convertAndSend("chat:room:" + roomId, message);
    }

    // 4. 대기방 레디 상태 업데이트
    @MessageMapping("/game/ready/{roomId}")
    public void sendReadyStatus(@DestinationVariable String roomId, @Payload GameEventReq event) {
        redisTemplate.convertAndSend("game:ready:" + roomId, event);
    }

    // 5. 게임 시작
    @MessageMapping("/game/start/{roomId}")
    public void startGame(@DestinationVariable String roomId) {
        long endTime = System.currentTimeMillis() + 30000; // 30초 후 종료
        GameEventReq startMessage = new GameEventReq(roomId, endTime);
        redisTemplate.convertAndSend("game:start:" + roomId, startMessage);
    }

    // 6. 게임 종료
    @MessageMapping("/game/end/{roomId}")
    public void endGame(@DestinationVariable String roomId) {
        redisTemplate.convertAndSend("game:end:" + roomId, "게임 종료");
    }

}
