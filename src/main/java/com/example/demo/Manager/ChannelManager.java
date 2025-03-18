package com.example.demo.Manager;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import static com.example.demo.config.WebSocketConstants.*;

@Component
public class ChannelManager {

    // 사용자에게 초기 채널을 구독하게 하기 위한 함수
    public void subscribeToInitialChannels(String username, WebSocketSession session) {
        String privateChannel = CHAT_PRIVATE_CHANNEL + username;

        // 채널 구독 (실제 구독 관리)
        addChannelToSession(session, CHAT_ALL_CHANNEL);
        addChannelToSession(session, privateChannel);
    }

    // 대기방 입장 시 처리 (전체 채팅방 해지, 대기방 채널 구독)
    public void subscribeToWaitingRoom(String username, String roomId, WebSocketSession session) {
        String roomChannel = CHAT_ROOM_CHANNEL + roomId;
        String readyStatusChannel = GAME_READY_CHANNEL + roomId;

        // 대기방 채널 및 레디 상태 채널 구독
        addChannelToSession(session, roomChannel);
        addChannelToSession(session, readyStatusChannel);

        // 전체 채팅방 해지
        removeChannelFromSession(session, CHAT_ALL_CHANNEL);
    }

    // 게임 시작 시 처리 (대기방 채널 해지, 게임 관련 채널 구독)
    public void subscribeToGame(String username, String gameId, WebSocketSession session) {
        String gameChatChannel = CHAT_ROOM_CHANNEL + gameId;
        String gameStartChannel = GAME_START_CHANNEL + gameId;

        // 게임 관련 채널 구독
        addChannelToSession(session, gameChatChannel);
        addChannelToSession(session, gameStartChannel);

        // 대기방 채널 및 1:1 채팅방 해지
        removeChannelFromSession(session, CHAT_ROOM_CHANNEL + gameId);
        removeChannelFromSession(session, CHAT_PRIVATE_CHANNEL + username);
    }

    // 게임 종료 시 처리 (대기방 채널 해지, 게임 채팅만 구독)
    public void subscribeToEndGame(String username, String gameId, WebSocketSession session) {
        String gameChatChannel = CHAT_ROOM_CHANNEL + gameId;

        // 대기방 채널 및 1:1 채팅방 구독 해지
        removeChannelFromSession(session, CHAT_ROOM_CHANNEL + gameId);
        removeChannelFromSession(session, CHAT_PRIVATE_CHANNEL + username);

        // 게임 채널 중 게임 채팅만 구독
        addChannelToSession(session, gameChatChannel);
    }

    // 대기방 탈출 시 처리 (대기방 채널 해지, 전체 채팅방 구독)
    public void subscribeToExitWaitingRoom(String username, WebSocketSession session) {
        // 대기방 채널 해지
        removeChannelFromSession(session, CHAT_ROOM_CHANNEL + "gameId");
        // 전체 채팅방 채널 구독
        addChannelToSession(session, CHAT_ALL_CHANNEL);
    }

    // 채널을 세션에 추가 (구독)
    private void addChannelToSession(WebSocketSession session, String channel) {
        // WebSocket 세션에 채널 추가 (구독 처리)
        session.getAttributes().put(channel, true); // 채널을 구독 리스트에 추가 (세션 속성에 채널 추가)
    }

    // 채널을 세션에서 제거 (구독 해지)
    private void removeChannelFromSession(WebSocketSession session, String channel) {
        // WebSocket 세션에서 채널 제거 (구독 해지)
        session.getAttributes().remove(channel); // 세션에서 채널 삭제
    }
}
