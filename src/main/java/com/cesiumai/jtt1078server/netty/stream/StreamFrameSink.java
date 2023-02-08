package com.cesiumai.jtt1078server.netty.stream;

/**
 * @author wuxiongbin
 */
public interface StreamFrameSink {
    boolean WriteFrame(StreamFrame frame);

    void CloseThisClient();
}