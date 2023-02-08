package com.cesiumai.jtt1078server.websocket.dto;

import java.io.Serializable;

/**
 * @author wuxiongbin
 */
public class QueryHistory implements Serializable {

    //逻辑通道号
    private byte channel;

    //开始时间
    private String startTime;

    //结束时间
    private String endTime;

    //报警标志 64BITS  8位
    private long alarmFlag;

    //音视频资源类型
    private byte mediaType;

    //码流类型
    private byte codeSteamType;

    //存储器类型
    private byte memoryType;

    public byte getChannel() {
        return channel;
    }

    public void setChannel(byte channel) {
        this.channel = channel;
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

    public long getAlarmFlag() {
        return alarmFlag;
    }

    public void setAlarmFlag(long alarmFlag) {
        this.alarmFlag = alarmFlag;
    }

    public byte getMediaType() {
        return mediaType;
    }

    public void setMediaType(byte mediaType) {
        this.mediaType = mediaType;
    }

    public byte getCodeSteamType() {
        return codeSteamType;
    }

    public void setCodeSteamType(byte codeSteamType) {
        this.codeSteamType = codeSteamType;
    }

    public byte getMemoryType() {
        return memoryType;
    }

    public void setMemoryType(byte memoryType) {
        this.memoryType = memoryType;
    }
}
