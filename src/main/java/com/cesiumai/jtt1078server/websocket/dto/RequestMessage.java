package com.cesiumai.jtt1078server.websocket.dto;

import java.io.Serializable;

/**
 * @author wuxiongbin
 */
public class RequestMessage<T> implements Serializable {

    /**
     * 终端唯一编号
     */
    private String terminalGuid;
    /**
     * 协议
     */
    private String protocol = "1078";
    /**
     * 指令
     */
    private Integer cmd;
    /**
     * 回复指令
     */
    private Integer replyCmd;
    /**
     * 记录标识
     */
    private String uuid;

    private T data;

    public String getTerminalGuid() {
        return terminalGuid;
    }

    public void setTerminalGuid(String terminalGuid) {
        this.terminalGuid = terminalGuid;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getCmd() {
        return cmd;
    }

    public void setCmd(Integer cmd) {
        this.cmd = cmd;
    }

    public Integer getReplyCmd() {
        return replyCmd;
    }

    public void setReplyCmd(Integer replyCmd) {
        this.replyCmd = replyCmd;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
