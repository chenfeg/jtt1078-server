package com.cesiumai.jtt1078server.kafka;

import com.cesiumai.jtt1078server.kafka.handler.ConsumerRecordHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;

@Slf4j
@Component
public class KafkaConsumer {

    @Resource
    private ConsumerRecordHandler consumerRecordHandler;

    @KafkaListener(topics = {"autopilot_1078_command_reply","autopilot_jtt808_2011_command_reply"})
    public void handleMessage(ConsumerRecord<?,?> record) {
        Object value = record.value();
        log.info("接收到来自kafka服务器的消息: {} , {} , {} ", record.topic(), value, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(record.timestamp()));
        consumerRecordHandler.handle(value);
    }
}
