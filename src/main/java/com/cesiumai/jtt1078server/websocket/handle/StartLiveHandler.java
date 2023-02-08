package com.cesiumai.jtt1078server.websocket.handle;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cesiumai.jtt1078server.kafka.KafkaProducer;
import com.cesiumai.jtt1078server.websocket.WebSocketSessionManager;
import com.cesiumai.jtt1078server.websocket.dto.RequestMessage;
import com.cesiumai.jtt1078server.websocket.dto.StartLive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

@Slf4j
@Component("StartLive")
public class StartLiveHandler implements MessageHandler {

    public static final String REGEX_STRING = "\\{TAG}";
    @Value("${jtt1078.server.ip}")
    private String serverAddress;
    @Value("${jtt1078.server.port}")
    private int serverPort;
    @Value("${play.rtmp.url}")
    private String rtmpUrl;
    @Value("${play.http.url}")
    private String url;
    @Value("${play.http.url}")
    private String httpsUrl;

    @Resource
    private KafkaProducer kafkaProducer;

    @Override
    public void handle(WebSocketSession session, JSONObject jsonObject) {
        JSONObject params = jsonObject.getJSONObject("params");
        String uuid = jsonObject.getString("uuid");
        String deviceId = params.getString("deviceId");
        RequestMessage<StartLive> message = new RequestMessage<>();
        message.setCmd(0x9101); // 实时音视频传输请求
        message.setTerminalGuid(deviceId);
        StartLive data = new StartLive();
        data.setIp(serverAddress);
        data.setTcpPort(serverPort);
        data.setUdpPort(0);
        data.setTransportProtocol("tcp");
        int channelNum = params.getIntValue("channel");
        data.setChannel((byte) channelNum);
        message.setUuid(uuid + "#" + channelNum);
        data.setMediaType((byte) params.getIntValue("type"));
        data.setStreamType((byte) params.getIntValue("streamType"));
        message.setData(data);
        String tag = deviceId + "-" + channelNum;
        if (WebSocketSessionManager.INSTANCE.isExistHisByTag(tag,1)) {
            reply(session, "发起直播失败，该通道正在播放历史视频", 2);
            return;
        }
        if (WebSocketSessionManager.INSTANCE.isExistLiveByTag(tag,1)) { // 已经存在订阅，直接返回播放地址
            log.info("已经存在订阅，直接返回播放地址");
            // 订阅视频数据
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("action", "Play");
            jsonObj.put("channelNum", channelNum);
            jsonObj.put("isLive", 0);
            jsonObj.put("status", 0);
            jsonObj.put("rtmpUrl", rtmpUrl.replaceAll(REGEX_STRING, tag));
            jsonObj.put("url", url.replaceAll(REGEX_STRING, tag));
            jsonObject.put("hlsUrl", httpsUrl.replaceAll(REGEX_STRING, tag));
            jsonObj.put("uuid", uuid);
            WebSocketSessionManager.INSTANCE.sendMessage(jsonObj.toString(),session);
            return;
        }
        WebSocketSessionManager.INSTANCE.addWebSocketSessionForTag(tag, session);
        kafkaProducer.send(JSON.toJSONString(message));
    }
}
