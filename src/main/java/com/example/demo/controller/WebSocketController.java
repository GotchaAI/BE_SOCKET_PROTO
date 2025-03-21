package com.example.demo.controller;

import com.example.demo.Manager.ChannelManager;
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
    private final ChannelManager channelManager;

    // WebSocket 첫 연결 시 초기 채널 관리
    @MessageMapping("/connect")
    public void onConnect(@Payload String nickName, @Header("simpSessionId") String sessionId) {
        channelManager.subscribeToInitialChannels(nickName, sessionId);
    }

    // 1. 전체 채팅방 메시지 전송
    @MessageMapping("/chat/all")
    public void sendMessageToAll(@Payload ChatMessageReq message, @Header("simpSessionId") String sessionId) {
        // 현재 세션이 구독한 채널 조회
        channelManager.getSubscribedChannels(sessionId);
        pubSubHandler.convertAndSend(CHAT_ALL_CHANNEL, message);
    }

    // 2. 귓속말 전송
    @MessageMapping("/chat/private")
    public void sendPrivateMessage(@Payload ChatMessageReq message) {
        pubSubHandler.convertAndSend(CHAT_PRIVATE_CHANNEL + message.nickName(), message);
    }

    // 3. 대기방 입장시 채널 관리
    @MessageMapping("/game/room/{roomId}")
    public void enterGameRoom(@DestinationVariable String roomId, @Header("simpSessionId") String sessionId) {
        channelManager.subscribeToWaitingRoom(roomId, sessionId);

        // 현재 세션이 구독한 채널 조회
        channelManager.getSubscribedChannels(sessionId);
    }

    // 4. 대기방 내 채팅
    @MessageMapping("/chat/room/{roomId}")
    public void sendRoomMessage(@DestinationVariable String roomId, @Payload ChatMessageReq message) {
        pubSubHandler.convertAndSend(CHAT_ROOM_CHANNEL + roomId, message);

    }

    // 4. 대기방 레디 상태 업데이트
    @MessageMapping("/game/ready/{roomId}")
    public void sendReadyStatus(@DestinationVariable String roomId, @Payload GameReadyStatus readyStatus) {
        pubSubHandler.convertAndSend(GAME_CHANNEL + roomId, readyStatus);
    }

    //5. 게임 시작 전 게임 정보(제시어) 전달
    @MessageMapping("/game/info/{roomId}")
    public void sendGameInfoBeforeStart(@DestinationVariable String roomId, String roundCount) { //라운드 수 같이 줘야 함.

        pubSubHandler.convertAndSend(GAME_CHANNEL + roomId, roundCount);
    }

    // 6. 게임 시작 ( 레디 상태가 최소 인원 넘는지 확인,
    @MessageMapping("/game/start/{roomId}")
    public void startGame(@DestinationVariable String roomId, @Payload String nickName,  @Header("simpSessionId") String sessionId) {
        long endTime = System.currentTimeMillis() + 30000; // 30초 후 종료
        GameEventReq startMessage = new GameEventReq(roomId, endTime);
        channelManager.subscribeToGame(nickName, roomId, sessionId);
        pubSubHandler.convertAndSend(GAME_CHANNEL + roomId, startMessage);
    }

    // 7. 게임 종료
    @MessageMapping("/game/end/{roomId}")
    public void endGame(@DestinationVariable String roomId, @Payload String nickName,  @Header("simpSessionId") String sessionId) {
        channelManager.subscribeToEndGame(nickName, roomId, sessionId);
        pubSubHandler.convertAndSend(GAME_CHANNEL + roomId, GAME_END_MESSAGE);
    }

    //8. 대기방 퇴장
    @MessageMapping("/game/exit/{roomId}")
    public void exitGameRoom(@DestinationVariable String roomId, @Header("simpSessionId") String sessionId) {
         channelManager.subscribeToExitWaitingRoom(roomId, sessionId);
    }

}
