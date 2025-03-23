let stompClient = null;
let username = "";
let roomId = ""; // 게임방 입장 후 설정
let isReady = false;

document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("joinButton").addEventListener("click", join);
    document.getElementById("joinRoomButton").addEventListener("click", joinRoom); // 게임방 입장 버튼 추가
    document.getElementById("sendChatButton").addEventListener("click", sendChatMessage);
    document.getElementById("sendGameChatButton").addEventListener("click", sendGameChatMessage);
    document.getElementById("readyButton").addEventListener("click", toggleReady);
    document.getElementById("startGameButton").addEventListener("click", startGame);
});

// ✅ 기본 웹소켓 연결 (닉네임 입력 후 입장)
function join() {
    username = document.getElementById("usernameInput").value.trim();

    if (!username) {
        alert("닉네임을 입력하세요!");
        return;
    }

    document.getElementById("chatSection").classList.remove("hidden");

    const socket = new SockJS("http://localhost:8080/ws-connect");
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log("✅ 웹소켓 연결 성공");

        // 전체 채팅 구독
        stompClient.subscribe("/sub/chat/all/", function (msg) {
            console.log("📩 [서버에서 받은 메시지]", msg.body);
            displayChatMessage(JSON.parse(msg.body));
        });

        // 서버에 연결 알림 (사용자 이름 전송)
        stompClient.send("/pub/connect", {}, JSON.stringify({nickName : username}));
    });
}

// ✅ 게임방 입장 (방 ID 입력 후 입장)
function joinRoom() {
    roomId = document.getElementById("roomIdInput").value.trim();

    if (!roomId) {
        alert("게임방 ID를 입력하세요!");
        return;
    }

    document.getElementById("gameSection").classList.remove("hidden");
    document.getElementById("currentRoomId").innerText = roomId;

    // ✅ 서버의 `@MessageMapping("/game/room/{roomId}")`에 WebSocket 요청 보내기
    stompClient.send(`/pub/game/room/${roomId}`, {}, JSON.stringify({ username }));

    // ✅ 게임방 채팅 구독
    stompClient.subscribe(`/sub/chat/room/${roomId}`, function (msg) {
        console.log("📝 [게임방 채팅 수신]", msg.body);
        displayGameChatMessage(JSON.parse(msg.body));
    });

    // ✅ 게임방 상태 메시지 구독 (레디 상태 변경, 게임 시작 등)
    stompClient.subscribe(`/sub/game/room/${roomId}`, function (msg) {
        console.log("🎮 [게임방 상태 메시지]", JSON.parse(msg.body));
        displayGameChatMessage(JSON.parse(msg.body));
    });
}


// ✅ 전체 채팅 전송
function sendChatMessage() {
    const message = document.getElementById("chatInput").value.trim();
    if (!message) return;

    if (message.startsWith("/")) {
        const [targetNick, ...msgParts] = message.substring(1).split(" ");
        const privateMsg = msgParts.join(" ");

        if (!targetNick || !privateMsg) {
            alert("귓속말 형식: /닉네임 메시지");
            return;
        }

        stompClient.send("/pub/chat/private", {}, JSON.stringify({
            from: username,
            to: targetNick,
            content: privateMsg
        }));

        // 보낸 사람에게는 내가 보낸 귓속말 표시
        displayChatMessage({
            nickName: `귓속말 → ${targetNick}`,
            content: privateMsg
        });

    } else {
        stompClient.send("/pub/chat/all", {}, JSON.stringify({ nickName: username, content: message }));
    }

    document.getElementById("chatInput").value = "";
}


// ✅ 게임방 채팅 전송
function sendGameChatMessage() {
    const message = document.getElementById("gameChatInput").value.trim();
    if (!message) return;

    if (message.startsWith("/")) {
        const [targetNick, ...msgParts] = message.substring(1).split(" ");
        const privateMsg = msgParts.join(" ");

        if (!targetNick || !privateMsg) {
            alert("귓속말 형식: /닉네임 메시지");
            return;
        }

        stompClient.send("/pub/chat/private", {}, JSON.stringify({
            from: username,
            to: targetNick,
            content: privateMsg
        }));

        displayGameChatMessage({
            nickName: `귓속말 → ${targetNick}`,
            content: privateMsg
        });

    } else {
        stompClient.send(`/pub/chat/room/${roomId}`, {}, JSON.stringify({ nickName: username, content: message }));
    }

    document.getElementById("gameChatInput").value = "";
}


// ✅ 레디 상태 변경 시 게임 채팅창에 로그 출력
function toggleReady() {
    isReady = !isReady;
    document.getElementById("readyButton").innerText = isReady ? "✅ 레디 해제" : "🔴 레디";

    const readyMessage = `${username}님이 ${isReady ? "레디 완료" : "레디 해제"} 상태로 변경되었습니다.`;

    stompClient.send(`/pub/game/ready/${roomId}`, {}, JSON.stringify({ nickName: username, isReady }));
    displayGameChatMessage({ nickName: "시스템", content: readyMessage });
}

// ✅ 게임 시작 요청 & 게임 채팅창에 로그 출력
function startGame() {
    stompClient.send(`/pub/game/start/${roomId}`, {}, JSON.stringify(username));
    displayGameChatMessage({ nickName: "시스템", content: "🚀 게임이 시작되었습니다!" });
}

// ✅ 전체 채팅 메시지 출력
function displayChatMessage(chatMsg) {
    const chatBox = document.getElementById("chatBox");
    const msgElement = document.createElement("p");
    msgElement.innerHTML = `<strong>${chatMsg.nickName}:</strong> ${chatMsg.content}`;
    chatBox.appendChild(msgElement);
    chatBox.scrollTop = chatBox.scrollHeight;
}

// ✅ 게임방 채팅 메시지 출력
function displayGameChatMessage(chatMsg) {
    const chatBox = document.getElementById("gameChatBox");
    const msgElement = document.createElement("p");
    msgElement.innerHTML = `<strong>${chatMsg.nickName}:</strong> ${chatMsg.content}`;
    chatBox.appendChild(msgElement);
    chatBox.scrollTop = chatBox.scrollHeight;
}
