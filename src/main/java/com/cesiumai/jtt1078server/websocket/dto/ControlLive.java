package com.cesiumai.jtt1078server.websocket.dto;

import java.io.Serializable;

/**
 * @author wuxiongbin
 */
public class ControlLive implements Serializable {
    // 逻辑通道号
    private byte channel;
    //控制指令
    private byte command;
    //关闭音视频类型
    private byte closeType;
    //切换码流类型
    private byte switchType;

    public byte getChannel() {
        return channel;
    }

    public void setChannel(byte channel) {
        this.channel = channel;
    }

    public byte getCommand() {
        return command;
    }

    public void setCommand(byte command) {
        this.command = command;
    }

    public byte getCloseType() {
        return closeType;
    }

    public void setCloseType(byte closeType) {
        this.closeType = closeType;
    }

    public byte getSwitchType() {
        return switchType;
    }

    public void setSwitchType(byte switchType) {
        this.switchType = switchType;
    }
}
