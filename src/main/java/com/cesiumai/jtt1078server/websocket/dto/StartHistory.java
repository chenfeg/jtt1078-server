package com.cesiumai.jtt1078server.websocket.dto;

import java.io.Serializable;

/**
 * @author wuxiongbin
 */
public class StartHistory implements Serializable {

    //服务器IP地址
    private String ip;

    //服务器音视频通道监听端口号 TCP  不使用传输置0
    private Integer tcpPort;

    //服务器音视频通道监听端口号 UDP 不使用传输置0
    private Integer udpPort;
    // 传输协议
    private String transportProtocol;
    //逻辑通道号
    private byte channel;

    //音视频类型
    private byte mediaType;

    //码流类型
    private byte streamType;

    //存储器类型
    private byte memoryType;

    //回放方式  0：正常回放；1：快进回放；2：关键帧快退回放；3：关键帧播放；4：单帧上传
    private byte playbackType;

    //快进或快退倍数     回放方式为1和2时，此字段内容有效，否则置0。
    //    0：无效；1：1倍；2：2倍3：4倍；4：8倍；5：16倍
    private byte playbackRatio;

    //开始时间
    private String startTime;

    //结束时间
    private String endTime;

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

    public byte getMemoryType() {
        return memoryType;
    }

    public void setMemoryType(byte memoryType) {
        this.memoryType = memoryType;
    }

    public byte getPlaybackType() {
        return playbackType;
    }

    public void setPlaybackType(byte playbackType) {
        this.playbackType = playbackType;
    }

    public byte getPlaybackRatio() {
        return playbackRatio;
    }

    public void setPlaybackRatio(byte playbackRatio) {
        this.playbackRatio = playbackRatio;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
