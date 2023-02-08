package com.cesiumai.jtt1078server.websocket.handle;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cesiumai.jtt1078server.kafka.KafkaProducer;
import com.cesiumai.jtt1078server.websocket.WebSocketSessionManager;
import com.cesiumai.jtt1078server.websocket.dto.ControlLive;
import com.cesiumai.jtt1078server.websocket.dto.RequestMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

@Component("ControlLive")
public class ControlLiveHandler implements MessageHandler {

    @Resource
    private KafkaProducer kafkaProducer;

    @Override
    public void handle(WebSocketSession session, JSONObject jsonObject) {
        JSONObject params = jsonObject.getJSONObject("params");
        String uuid = jsonObject.getString("uuid");
        String deviceId = params.getString("deviceId");
        RequestMessage<ControlLive> message = new RequestMessage<>();
        message.setUuid(uuid);
        message.setCmd(0x9102); // 音视频实时控制
        message.setTerminalGuid(deviceId);
        ControlLive data = new ControlLive();
        int channelNum = params.getIntValue("channel");
        data.setChannel((byte) channelNum);
        int command = params.getIntValue("command");
        data.setCommand((byte) command);
        data.setCloseType((byte) params.getIntValue("closeType"));
        data.setSwitchType((byte) params.getIntValue("switchType"));
        message.setData(data);
        String tag = deviceId + "-" + channelNum;
        if (WebSocketSessionManager.INSTANCE.isExistLiveByTag(tag,2)) {
            reply(session, "控制失败：其它用户同时使用，不允许切换", 3);
            return;
        }
        WebSocketSessionManager.INSTANCE.addWebSocketSessionForTag(tag, session);
        String jsonMessage = JSON.toJSONString(message);
        kafkaProducer.send(jsonMessage);
        if (command == 0) {
            WebSocketSessionManager.INSTANCE.removeLiveTags(tag,session);
            WebSocketSessionManager.INSTANCE.closeFfmpegTheadForTag(tag);
        }
    }
}
