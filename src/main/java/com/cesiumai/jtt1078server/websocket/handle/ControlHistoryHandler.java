package com.cesiumai.jtt1078server.websocket.handle;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cesiumai.jtt1078server.kafka.KafkaProducer;
import com.cesiumai.jtt1078server.netty.ffmpeg.H264StreamHub;
import com.cesiumai.jtt1078server.websocket.WebSocketSessionManager;
import com.cesiumai.jtt1078server.websocket.dto.ControlHistory;
import com.cesiumai.jtt1078server.websocket.dto.RequestMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

@Slf4j
@Component("ControlHist")
public class ControlHistoryHandler implements MessageHandler {

    @Resource
    private KafkaProducer kafkaProducer;

    @Override
    public void handle(WebSocketSession session, JSONObject jsonObject) {
        JSONObject params = jsonObject.getJSONObject("params");
        String uuid = jsonObject.getString("uuid");
        String deviceId = params.getString("deviceId");
        RequestMessage<ControlHistory> message = new RequestMessage<>();
        message.setUuid(uuid);
        message.setCmd(0x9202); // 历史音视频控制
        message.setTerminalGuid(deviceId);
        ControlHistory data = new ControlHistory();
        int channelNum = params.getIntValue("channel");
        data.setChannel((byte) channelNum);
        int command = params.getIntValue("command");
        data.setPlayController((byte) command);
        data.setPlaybackRatio((byte) params.getIntValue("playBackRatio"));
        String positionTime = params.getString("fastPosition");
        if (5 == command && (StringUtils.isBlank(positionTime) || positionTime.trim().equals("undefined") || !positionTime.matches("^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])\\s+(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d$"))) {
            reply(session, "时间参数错误", 6);
            return;
        }
        data.setPositionTime(positionTime);
        message.setData(data);
        String tag = deviceId + "-" + channelNum;
        if (WebSocketSessionManager.INSTANCE.isExistHisByTag(tag, 2)) {
            reply(session, "控制失败：其它用户同时使用，不允许切换", 3);
            return;
        }
        WebSocketSessionManager.INSTANCE.addWebSocketSessionForTag(tag, session);
        String jsonMessage = JSON.toJSONString(message);
        log.info("发送点播控制指令消息：{}", jsonMessage);
        kafkaProducer.send(jsonMessage);
        if (2 == command) {
            WebSocketSessionManager.INSTANCE.removeHisTags(tag, session);
            H264StreamHub.closeFfmpegTheadForTag(tag);
            //WebSocketSessionManager.INSTANCE.closeFfmpegTheadForTag(tag);
        }
    }
}
