package com.cesiumai.jtt1078server.websocket.handle;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cesiumai.jtt1078server.kafka.KafkaProducer;
import com.cesiumai.jtt1078server.websocket.dto.QueryProperties;
import com.cesiumai.jtt1078server.websocket.dto.RequestMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

@Component("QueryAttribute")
public class QueryAttributeHandler implements MessageHandler {
    @Resource
    private KafkaProducer kafkaProducer;

    @Override
    public void handle(WebSocketSession session, JSONObject jsonObject) {
        String uuid = jsonObject.getString("uuid");
        String deviceId = jsonObject.getString("deviceId");
        RequestMessage<QueryProperties> requestMessage = new RequestMessage<>();
        requestMessage.setUuid(uuid);
        requestMessage.setCmd(0x9003); // 实时音视频传输请求
        requestMessage.setTerminalGuid(deviceId);
        QueryProperties queryProperties = new QueryProperties();
        requestMessage.setData(queryProperties);
        kafkaProducer.send(JSON.toJSONString(requestMessage));
    }
}
