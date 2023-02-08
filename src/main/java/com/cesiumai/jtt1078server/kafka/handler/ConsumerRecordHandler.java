package com.cesiumai.jtt1078server.kafka.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cesiumai.jtt1078server.netty.ffmpeg.PushStreamService;
import com.cesiumai.jtt1078server.websocket.WebSocketSessionManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;

@Slf4j
@Component
public class ConsumerRecordHandler {
    public static final String REGEX_STRING = "\\{TAG}";
    @Value("${play.rtmp.url}")
    private String rtmpUrl;
    @Value("${play.http.url}")
    private String url;
    @Value("${play.https.url}")
    private String httpsUrl;
    @Value("${http.flv.port}")
    private int httpFlvPort;
    @Value("${ffmpeg.path}")
    private String ffmpegPath;
    @Value("${ffmpeg.debug}")
    private boolean isDebug;

    private PushStreamService pushStreamService;

    public void handle(Object message) {
        if (Objects.isNull(message) || !JSON.isValidObject(message.toString())) {
            log.error("接收到的kafka消息格式不是json,message: {}", message.toString());
            return;
        }
        JSONObject jsonObject = JSON.parseObject(message.toString());
        if (!jsonObject.containsKey("uuid")) {
            log.error("接收到的kafka消息中没有uuid, json message: {}", jsonObject);
            return;
        }
        String replyCmd = jsonObject.getString("replyCmd");
        switch (replyCmd) {
            case "36867": // 处理查询终端音视频属性的响应
                terminalAudioAndVideoPropertiesResponse(jsonObject);
                break;
            case "37121": // 处理信令服务器回复0x9010 实时音视频传输请求
                audioAndVideoTransmissionResponse(jsonObject);
                break;
            case "4613": // 查询历史音视频消息和远程录像回放消息都回复0x1205，{uuid}#1 表示查询，{uuid}#2表示回放
                historicalAudioAndVideoResponse(jsonObject);
                break;
            case "37378": // 远程录像回放控制
            case "37122": // 实时音视频传输控制
                audioAndVideoControlResponse(jsonObject);
                break;
            default:
                log.error("未处理的kafka消息");
        }
    }

    private void terminalAudioAndVideoPropertiesResponse(JSONObject replayJsonObj) {
        String uuid = replayJsonObj.getString("uuid");
        JSONObject dataJsonObj = replayJsonObj.getJSONObject("data");
        WebSocketSession webSocketSession = WebSocketSessionManager.INSTANCE.getWebSocketSessionByUUID(uuid);
        if (null == webSocketSession) {
            log.error("接收到的uuid对应的通道Channel不存在");
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", "QueryAttribute");
        jsonObject.put("maxVideoChannelNum", dataJsonObj.getIntValue("maxVideoChannelNum"));
        jsonObject.put("maxAudioChannelNum", dataJsonObj.getIntValue("maxAudioChannelNum"));
        jsonObject.put("supportAudioOutput", dataJsonObj.getIntValue("supportAudioOutput"));
        WebSocketSessionManager.INSTANCE.sendMessage(jsonObject.toString(), webSocketSession);
    }

    // 实时音视频传输请求响应
    private void audioAndVideoTransmissionResponse(JSONObject replayJsonObj) {
        String uuid = replayJsonObj.getString("uuid");
        String s = StringUtils.substringAfterLast(uuid, "#");
        uuid = StringUtils.substringBeforeLast(uuid, "#");
        JSONObject dataJsonObj = replayJsonObj.getJSONObject("data");
        WebSocketSession webSocketSession = WebSocketSessionManager.INSTANCE.getWebSocketSessionByUUID(uuid);
        int resultCode = dataJsonObj.getIntValue("resultCode");
        if (null != webSocketSession && StringUtils.isNotBlank(s)) {
            String tag = WebSocketSessionManager.INSTANCE.getDeviceIdByWebSocketSession(webSocketSession) + "-" + s;
            WebSocketSessionManager.INSTANCE.addLiveWebSocketSessionForTag(tag, webSocketSession);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "Play");
            jsonObject.put("channelNum", s);
            jsonObject.put("isLive", 0);
            jsonObject.put("rtmpUrl", rtmpUrl.replaceAll(REGEX_STRING, tag));
            jsonObject.put("url", url.replaceAll(REGEX_STRING, tag));
            jsonObject.put("httpsUrl", httpsUrl.replaceAll(REGEX_STRING, tag));
            jsonObject.put("status", resultCode);
            jsonObject.put("uuid", uuid);
            WebSocketSessionManager.INSTANCE.sendMessage(jsonObject.toString(), webSocketSession);
            pushStreamService = new PushStreamService(tag, httpFlvPort, ffmpegPath, isDebug);
            pushStreamService.start();
            WebSocketSessionManager.INSTANCE.addFfmpegTheadForTag(tag,pushStreamService);
        }
    }

    // 播放音视频控制请求响应
    private void audioAndVideoControlResponse(JSONObject replayJsonObj) {
        String uuid = replayJsonObj.getString("uuid");
        JSONObject dataJsonObj = replayJsonObj.getJSONObject("data");
        int resultCode = dataJsonObj.getIntValue("resultCode");
        WebSocketSession webSocketSession = WebSocketSessionManager.INSTANCE.getWebSocketSessionByUUID(uuid);
        if (webSocketSession != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "Response");
            jsonObject.put("uuid", uuid);
            jsonObject.put("status", resultCode);
            WebSocketSessionManager.INSTANCE.sendMessage(jsonObject.toString(), webSocketSession);
        }
    }

    // 查询历史音视频请求响应
    private void historicalAudioAndVideoResponse(JSONObject replayJsonObj) {
        String uuid = replayJsonObj.getString("uuid");
        String s = StringUtils.substringAfterLast(uuid, "#");
        JSONObject dataJsonObj = replayJsonObj.getJSONObject("data");
        if (StringUtils.isBlank(s)) {
            WebSocketSession webSocketSession = WebSocketSessionManager.INSTANCE.getWebSocketSessionByUUID(uuid);
            if (null != webSocketSession) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("action", "ReplyQueryHist");
                jsonObject.put("uuid", uuid);
                String deviceId = WebSocketSessionManager.INSTANCE.getDeviceIdByWebSocketSession(webSocketSession);
                if (StringUtils.isBlank(deviceId)) {
                    log.error("无法通过channel获取到设备id");
                    return;
                }
                jsonObject.put("deviceId", deviceId);
                jsonObject.put("mediaList", dataJsonObj.getString("mediaList"));
                WebSocketSessionManager.INSTANCE.sendMessage(jsonObject.toString(), webSocketSession);
            }
        } else {
            uuid = StringUtils.substringBeforeLast(uuid, "#");
            WebSocketSession webSocketSession = WebSocketSessionManager.INSTANCE.getWebSocketSessionByUUID(uuid);
            if(null==webSocketSession){
                log.error("接收到的uuid对应的通道Channel不存在");
                return;
            }
            String terminalGuid = WebSocketSessionManager.INSTANCE.getDeviceIdByWebSocketSession(webSocketSession);
            String tag = terminalGuid + "-" + s;
            if (!dataJsonObj.getJSONArray("mediaList").isEmpty()) {
                WebSocketSessionManager.INSTANCE.addHisWebSocketSessionForTag(tag, webSocketSession);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("action", "Play");
                jsonObject.put("channelNum", s);
                jsonObject.put("isLive", 1);
                jsonObject.put("rtmpUrl", rtmpUrl.replaceAll(REGEX_STRING, tag));
                jsonObject.put("status", 0);
                jsonObject.put("url", url.replaceAll(REGEX_STRING, tag));
                jsonObject.put("httpsUrl", httpsUrl.replaceAll(REGEX_STRING, tag));
                jsonObject.put("uuid", uuid);
                WebSocketSessionManager.INSTANCE.sendMessage(jsonObject.toString(), webSocketSession);
                pushStreamService = new PushStreamService(tag, httpFlvPort, ffmpegPath, isDebug);
                pushStreamService.start();
                WebSocketSessionManager.INSTANCE.addFfmpegTheadForTag(tag,pushStreamService);
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("action", "Response");
                jsonObject.put("uuid", uuid);
                jsonObject.put("status", 7);
                jsonObject.put("message", "该时间段的历史音视频不存在，请重新选择时间");
                WebSocketSessionManager.INSTANCE.sendMessage(jsonObject.toString(), webSocketSession);
            }
        }
    }
}
