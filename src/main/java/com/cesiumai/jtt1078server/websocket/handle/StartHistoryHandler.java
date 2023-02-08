package com.cesiumai.jtt1078server.websocket.handle;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cesiumai.jtt1078server.kafka.KafkaProducer;
import com.cesiumai.jtt1078server.websocket.WebSocketSessionManager;
import com.cesiumai.jtt1078server.websocket.dto.RequestMessage;
import com.cesiumai.jtt1078server.websocket.dto.StartHistory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.util.Arrays;

@Slf4j
@Component("StartHist")
public class StartHistoryHandler implements MessageHandler {
    public static final String REGEX_STRING = "\\{TAG}";
    @Value("${jtt1078.server.ip}")
    private String serverAddress;
    @Value("${jtt1078.server.port}")
    private int serverPort;
    @Value("${play.rtmp.url}")
    private String rtmpUrl;
    @Value("${play.http.url}")
    private String url;
    @Value("${play.https.url}")
    private String httpsUrl;

    @Resource
    private KafkaProducer kafkaProducer;

    @Override
    public void handle(WebSocketSession session, JSONObject jsonObject) {
        JSONObject params = jsonObject.getJSONObject("params");
        String uuid = jsonObject.getString("uuid");
        String deviceId = params.getString("deviceId");
        RequestMessage<StartHistory> message = new RequestMessage<>();
        message.setCmd(0x9201); // 历史音视频传输请求
        message.setTerminalGuid(deviceId);
        StartHistory data = new StartHistory();
        data.setIp(serverAddress);
        data.setTcpPort(serverPort);
        data.setUdpPort(0);
        data.setTransportProtocol("tcp");
        int channelNum = params.getIntValue("channel");
        String tag = deviceId + "-" + channelNum;
        if (WebSocketSessionManager.INSTANCE.isExistLiveByTag(tag, 1)) {
            reply(session, "发起播放历史视频失败，该通道正在进行直播", 2);
            return;
        }
        if (WebSocketSessionManager.INSTANCE.isExistHisByTag(tag, 1)) { // 已经存在订阅，直接返回播放历史视频地址
            log.info("已经存在订阅，直接返回播放历史视频地址");
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("action", "Play");
            jsonObj.put("channelNum", channelNum);
            jsonObj.put("isLive", 1);
            jsonObj.put("status", 0);
            jsonObj.put("rtmpUrl", rtmpUrl.replaceAll(REGEX_STRING, tag));
            jsonObj.put("url", url.replaceAll(REGEX_STRING, tag));
            jsonObject.put("httpsUrl", httpsUrl.replaceAll(REGEX_STRING, tag));
            jsonObj.put("uuid", uuid);
            WebSocketSessionManager.INSTANCE.sendMessage(jsonObj.toString(), session);
            return;
        }
        message.setUuid(uuid + "#" + channelNum);
        data.setChannel((byte) channelNum);
        data.setMediaType((byte) params.getIntValue("mediaType"));
        data.setStreamType((byte) params.getIntValue("streamType"));
        data.setMemoryType((byte) params.getIntValue("memoryType"));
        data.setPlaybackType((byte) params.getIntValue("playBackType"));
        data.setPlaybackRatio((byte) params.getIntValue("playBackRatio"));
        String startTime = params.getString("startTime");
        String endTime = params.getString("endTime");
        if (Arrays.stream(new String[]{startTime, endTime}).anyMatch(t -> StringUtils.isBlank(t) || t.trim().equals("undefined")|| !t.matches("^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])\\s+(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d$"))) {
            reply(session, "时间参数错误", 6);
            return;
        }

        data.setStartTime(startTime);
        data.setEndTime(endTime);
        message.setData(data);
        WebSocketSessionManager.INSTANCE.addWebSocketSessionForTag(tag, session);
        kafkaProducer.send(JSON.toJSONString(message));
    }
}
