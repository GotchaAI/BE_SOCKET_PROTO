package com.example.demo.config;

import java.util.StringTokenizer;

public interface WebSocketConstants {
    //채팅 관련 채널
    String CHAT_PREFIX = "chat:";
    String CHAT_ALL_CHANNEL = "chat:all";
    String CHAT_PRIVATE_CHANNEL = "chat:private:";//+채팅 보낼 상대 닉네임
    String CHAT_ROOM_CHANNEL = "chat:room:";//+해당 방 고유 id(숫자 4자리)

    //게임 관련 채널
    String GAME_PREFIX = "game:";
    String GAME_READY_CHANNEL = GAME_PREFIX+"ready:";//+boolan 값
    String GAME_INFO_CHANNEL = GAME_PREFIX+"info:";//+해당 방 고유 id(숫자 4자리)
    String GAME_START_CHANNEL = GAME_PREFIX+"start:";//+해당 방 고유 id(숫자 4자리)
    String GAME_END_CHANNEL = GAME_PREFIX+"end:";//+해당 방 고유 id(숫자 4자리)
    String GAME_END_MESSAGE = "게임종료";
}
