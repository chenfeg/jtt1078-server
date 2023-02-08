package com.cesiumai.jtt1078server.websocket.dto;

import java.io.Serializable;

/**
 * @author wuxiongbin
 */
public class StartLive implements Serializable {

    // 服务器1P地址
    private String ip;
    //端口
    private Integer tcpPort;
    //端口是tcp还是udp
    private Integer udpPort;
    // 传输协议
    private String transportProtocol;
    // 逻辑通道号
    private byte channel;
    // 数据类型 0：音视频，1：视频，2：双向对讲，3：监听，4：中心广播，5：透传
    private byte mediaType;
    // 码流类型 0：主码流，1：子码流
    private byte streamType;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(Integer tcpPort) {
        this.tcpPort = tcpPort;
    }

    public Integer getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(Integer udpPort) {
        this.udpPort = udpPort;
    }

    public String getTransportProtocol() {
        return transportProtocol;
    }

    public void setTransportProtocol(String transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    public byte getChannel() {
        return channel;
    }

    public void setChannel(byte channel) {
        this.channel = channel;
    }

    public byte getMediaType() {
        return mediaType;
    }

    public void setMediaType(byte mediaType) {
        this.mediaType = mediaType;
    }

    public byte getStreamType() {
        return streamType;
    }

    public void setStreamType(byte streamType) {
        this.streamType = streamType;
    }
}
