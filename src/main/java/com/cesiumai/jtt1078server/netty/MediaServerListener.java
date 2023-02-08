package com.cesiumai.jtt1078server.netty;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author wuxiongbin
 */
@Slf4j
@Component
public class MediaServerListener implements ApplicationRunner {

    @Value("${http.flv.port}")
    private int httpFlvPort;
    @Value("${jtt1078.server.port}")
    private int jtt1078Port;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        NettyMediaServer.start(httpFlvPort, jtt1078Port);
    }
}
