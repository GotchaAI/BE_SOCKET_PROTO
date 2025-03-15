package com.example.demo.config;

import com.example.demo.listener.RedisSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private String port;

    //Redis와의 연결을 생성.
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(Integer.parseInt(port));
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration);
        return lettuceConnectionFactory;
    }

    //Redis Pub/Sub에서 메시지를 리스닝하는 컨테이너 -> (구독자가 어떤 메시지를 받을지 관리)
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        //컨테이너는 각 채널의 리스닝어뎁터를 받음 -> 해당 컨테이너가 모든 채널 관리가 가능함!
        container.addMessageListener(listenerAdapter, new PatternTopic("chat:all")); //채팅 채널
        container.addMessageListener(listenerAdapter, new PatternTopic("chat:private:*")); //귓말 채널 (*에는 닉네임 들어감)
        container.addMessageListener(listenerAdapter, new PatternTopic("chat:room:*")); //대기방 채널 (*에는 고유 방 코드 들어감)
        container.addMessageListener(listenerAdapter, new PatternTopic("game:*")); //인게임 관련 채널 (*에는 고유 방 코드 들어감) -> 필요 없을 것 같음

        return container;
    }

    // Redis에서 메시지를 수신하면 RedisSubscriber 클래스의 onMessage 메서드를 호출하도록 설정
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
