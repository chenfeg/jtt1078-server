package com.cesiumai.jtt1078server.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class KafkaProducer {

    @Resource
    private KafkaTemplate kafkaTemplate;

    public void send(String message) {
        kafkaTemplate.send("autopilot_1078_command", message);
    }
}
