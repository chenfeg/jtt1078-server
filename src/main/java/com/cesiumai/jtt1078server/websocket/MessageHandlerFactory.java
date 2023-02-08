package com.cesiumai.jtt1078server.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cesiumai.jtt1078server.configuration.ApplicationContextHelper;
import com.cesiumai.jtt1078server.websocket.handle.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

@Slf4j
@Component
public class MessageHandlerFactory {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ApplicationContextHelper applicationContextHelper;

    public void handleMessage(WebSocketSession session, JSONObject jsonObject) {
        String action = jsonObject.getString("action");
        Object bean = applicationContextHelper.getBean(action);
        if (ObjectUtils.isEmpty(bean)) {
            log.error("没有找到对应的处理bean");
            return;
        }
        MessageHandler handler = (MessageHandler) bean;
        if(jsonObject.containsKey("deviceId")&& online(jsonObject.getString("deviceId"))){
            handler.reply(session, "设备不在线", 1);
            return;
        }
        if (jsonObject.containsKey("params") && online(jsonObject.getJSONObject("params").getString("deviceId"))) {
            handler.reply(session, "设备不在线", 1);
            return;
        }
        handler.handle(session, jsonObject);
    }

    private boolean online(String deviceId) {
        String key = "device_status_" + deviceId;
        String value = stringRedisTemplate.opsForValue().get(key);
        return !JSON.isValidObject(value) || !JSON.parseObject(value).getBoolean("online");
    }

}
