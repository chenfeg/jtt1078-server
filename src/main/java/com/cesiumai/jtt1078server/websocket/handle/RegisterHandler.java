package com.cesiumai.jtt1078server.websocket.handle;

import com.alibaba.fastjson2.JSONObject;
import com.cesiumai.jtt1078server.websocket.WebSocketSessionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component("Hello")
public class RegisterHandler implements MessageHandler {

    @Override
    public void handle(WebSocketSession session, JSONObject jsonObject) {
        jsonObject.remove("terminal");
        WebSocketSessionManager.INSTANCE.sendMessage(jsonObject.toString(),session);
    }
}
