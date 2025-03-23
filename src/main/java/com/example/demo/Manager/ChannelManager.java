package com.example.demo.Manager;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static com.example.demo.config.WebSocketConstants.*;

@Component
@RequiredArgsConstructor
public class ChannelManager {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CHANNEL_PREFIX = "channel:"; // Redis Key Prefix


    // 사용자에게 초기 채널을 구독하게 하기 위한 함수 (전체 채팅방 + 귓말 구독)
    public void subscribeToInitialChannels(String username, String sessionId) {
        subscribeToChannel(sessionId, CHAT_ALL_CHANNEL);
        subscribeToChannel(sessionId, CHAT_PRIVATE_CHANNEL + username);
    }

    // 대기방 입장 시 처리 (전체 채팅방 해지 / 대기방 채널 + 게임 채널 구독)
    public void subscribeToWaitingRoom(String roomId, String sessionId) {
        subscribeToChannel(sessionId, CHAT_ROOM_CHANNEL + roomId);
        subscribeToChannel(sessionId, GAME_CHANNEL + roomId);

        unsubscribeFromChannel(sessionId, CHAT_ALL_CHANNEL);
    }

    // 게임 시작 시 처리 (대기방 채팅 + 귓말 채널 해지)
    public void subscribeToGame(String nickName, String roomId, String sessionId) {
        unsubscribeFromChannel(sessionId, CHAT_ROOM_CHANNEL + roomId);
        unsubscribeFromChannel(sessionId, CHAT_PRIVATE_CHANNEL + nickName);
    }

    // 게임 종료 시 처리  (대기방 채팅 + 귓말 채널 구독)
    public void subscribeToEndGame(String nickName, String roomId, String sessionId) {
        subscribeToChannel(sessionId, CHAT_ROOM_CHANNEL + roomId);
        subscribeToChannel(sessionId, CHAT_PRIVATE_CHANNEL + nickName);
    }

    // 대기방 퇴장 시 처리 (대기방 채널 + 게임 채널 해지 / 전체 채팅방 구독)
    public void subscribeToExitWaitingRoom(String roomId, String sessionId) {
        unsubscribeFromChannel(sessionId, CHAT_ROOM_CHANNEL + roomId);
        unsubscribeFromChannel(sessionId, GAME_CHANNEL + roomId);

        subscribeToChannel(sessionId, CHAT_ALL_CHANNEL);
    }

    // 채널을 세션에 추가 (구독)
    private void subscribeToChannel(String sessionId, String channel) {
        redisTemplate.opsForSet().add(CHANNEL_PREFIX + sessionId, channel);
        System.out.println("✅ 채널 구독: " + sessionId + " → " + channel);

        // 🚨 Redis에 값이 정상적으로 저장되는지 확인
        Set<String> subscribedChannels = getSubscribedChannels(sessionId);
        System.out.println("🔍 [Redis 저장 확인] 세션 " + sessionId + "이 구독한 채널: " + subscribedChannels);
    }

    // 채널을 세션에서 제거 (구독 해지)
    private void unsubscribeFromChannel(String sessionId, String channel) {
        redisTemplate.opsForSet().remove(CHANNEL_PREFIX + sessionId, channel);
        System.out.println("❌ 채널 해지: " + sessionId + " → " + channel);
    }

    // 세션이 구독한 모든 채널 조회
    public Set<String> getSubscribedChannels(String sessionId) {
        Set<Object> rawChannels = redisTemplate.opsForSet().members(CHANNEL_PREFIX + sessionId);

        if (rawChannels == null || rawChannels.isEmpty()) {
            return Set.of();
        }

        System.out.println("구독 채널 = " + rawChannels);

        return rawChannels.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    // 세션 삭제 시(로그아웃) 모든 채널 구독 해지
    public void removeAllSubscriptions(String sessionId) {
        redisTemplate.delete(CHANNEL_PREFIX + sessionId);
        System.out.println("🗑️ 세션의 모든 구독 해지: " + sessionId);
    }

    public RedisTemplate<String, Object> getRedisTemplate () {
        return redisTemplate;
    }
}
