package com.cesiumai.jtt1078server.websocket.handle;

import com.alibaba.fastjson2.JSONObject;
import com.cesiumai.jtt1078server.websocket.WebSocketSessionManager;
import org.springframework.web.socket.WebSocketSession;

public interface MessageHandler {

    void handle(WebSocketSession session, JSONObject jsonObject);

    default void reply(WebSocketSession session, String message, int status) {
        JSONObject replayJson = new JSONObject();
        replayJson.put("action", "Response");
        replayJson.put("status", status);
        replayJson.put("message", message);
        WebSocketSessionManager.INSTANCE.sendMessage(replayJson.toString(),session);
    }
}
