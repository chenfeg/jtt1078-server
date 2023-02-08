package com.cesiumai.jtt1078server.websocket.dto;

import java.io.Serializable;

/**
 * @author wuxiongbin
 */
public class LiveStatus implements Serializable {

    //逻辑通道号
    private byte channel;
    //丢包率  当前传输通道的丢包率,数值乘以100之后取整数部分
    private byte packetLossRate;

    public byte getChannel() {
        return channel;
    }

    public void setChannel(byte channel) {
        this.channel = channel;
    }

    public byte getPacketLossRate() {
        return packetLossRate;
    }

    public void setPacketLossRate(byte packetLossRate) {
        this.packetLossRate = packetLossRate;
    }
}
