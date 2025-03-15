package com.example.demo.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

// Controller -> Redis로 받은 메시지를 다른 사용자들에게(WebSocket으로) 전달
@Service
public class RedisSubscriber implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern); // 어떤 채널에서 메시지가 왔는지 확인
        String msg = message.toString(); // 메시지 내용 가져오기

        if (channel.startsWith("chat:all")) {
            System.out.println("📢 전체 채팅: " + msg);
        } else if (channel.startsWith("chat:private:")) {
            System.out.println("📩 귓속말 수신: " + msg);
        } else if (channel.startsWith("chat:room:")) {
            System.out.println("🏠 대기방 채팅: " + msg);
        } else {
            System.out.println("📜 기타 메시지 (" + channel + "): " + msg);
        }
    }
}