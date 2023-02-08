package com.cesiumai.jtt1078server.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import javax.annotation.Resource;

@Slf4j
@Component
public class WebSocketHandler extends AbstractWebSocketHandler {

    @Resource
    private MessageHandlerFactory messageHandlerFactory;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        WebSocketSessionManager.INSTANCE.removeAllBySession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("WebSocketSession ===> {}", session);
        log.info("received websocket message : {}", JSON.toJSONString(message));
        String payload = message.getPayload();
        if (!JSON.isValidObject(payload)) {
            log.error("接收到的json消息格式错误");
            return;
        }
        JSONObject jsonObject = JSON.parseObject(payload);
        if (!jsonObject.containsKey("uuid")) {
            log.error("接收到的json({})消息中不存在必须的uuid字段", payload);
            return;
        }
        WebSocketSessionManager.INSTANCE.addWebSocketSessionForUUID(jsonObject.getString("uuid"), session);
        messageHandlerFactory.handleMessage(session, jsonObject);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        WebSocketSessionManager.INSTANCE.removeAllBySession(session);
    }
}
