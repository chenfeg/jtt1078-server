package com.cesiumai.jtt1078server.websocket.dto;

import java.io.Serializable;

/**
 * @author wuxiongbin
 */
public class ControlHistory implements Serializable {

    private byte channel;//音视频通道号

    private byte playController;//回放控制

    private byte playbackRatio;//回放控制为3和4时有效 否则置位0

    private String positionTime;//拖动回放位子BCD[6] YY-MM-DD-HH-MM-SS 回放控制为5时有效

    public byte getChannel() {
        return channel;
    }

    public void setChannel(byte channel) {
        this.channel = channel;
    }

    public byte getPlayController() {
        return playController;
    }

    public void setPlayController(byte playController) {
        this.playController = playController;
    }

    public byte getPlaybackRatio() {
        return playbackRatio;
    }

    public void setPlaybackRatio(byte playbackRatio) {
        this.playbackRatio = playbackRatio;
    }

    public String getPositionTime() {
        return positionTime;
    }

    public void setPositionTime(String positionTime) {
        this.positionTime = positionTime;
    }
}
