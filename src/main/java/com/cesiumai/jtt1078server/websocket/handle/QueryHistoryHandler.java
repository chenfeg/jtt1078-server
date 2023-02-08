package com.cesiumai.jtt1078server.websocket.handle;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cesiumai.jtt1078server.kafka.KafkaProducer;
import com.cesiumai.jtt1078server.websocket.WebSocketSessionManager;
import com.cesiumai.jtt1078server.websocket.dto.QueryHistory;
import com.cesiumai.jtt1078server.websocket.dto.RequestMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Objects;

@Component("QueryHist")
public class QueryHistoryHandler implements MessageHandler {

    @Resource
    private KafkaProducer kafkaProducer;

    @Override
    public void handle(WebSocketSession session, JSONObject jsonObject) {
        JSONObject params = jsonObject.getJSONObject("params");
        String uuid = jsonObject.getString("uuid");
        String deviceId = params.getString("deviceId");
        RequestMessage<QueryHistory> message = new RequestMessage<>();
        message.setUuid(uuid);
        message.setCmd(0x9205); // 查询历史音视频
        message.setTerminalGuid(deviceId);
        QueryHistory data = new QueryHistory();
        if (Objects.nonNull(params.get("alarmFlag"))) {
            data.setAlarmFlag(params.getLong("alarmFlag"));
        } else {
            data.setAlarmFlag(0);
        }
        int channelNum = params.getIntValue("channel");
        data.setChannel((byte) channelNum);
        if (Objects.nonNull(params.get("codecSteamType"))) {
            data.setCodeSteamType((byte) params.getIntValue("codecSteamType"));
        } else {
            data.setCodeSteamType((byte) 0);
        }
        data.setMediaType((byte) params.getIntValue("mediaType"));
        data.setMemoryType((byte) params.getIntValue("memoryType"));
        String startTime = params.getString("startTime");
        String endTime = params.getString("endTime");
        if (Arrays.stream(new String[]{startTime, endTime}).anyMatch(t -> StringUtils.isBlank(t) || t.trim().equals("undefined"))) {
            reply(session, "时间参数错误", 6);
            return;
        }
        data.setStartTime(startTime);
        data.setEndTime(endTime);
        message.setData(data);
        WebSocketSessionManager.INSTANCE.addWebSocketSessionForTag(deviceId + "-" + channelNum, session);
        kafkaProducer.send(JSON.toJSONString(message));
    }
}
