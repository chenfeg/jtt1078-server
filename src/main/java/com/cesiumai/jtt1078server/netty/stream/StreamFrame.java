package com.cesiumai.jtt1078server.netty.stream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;

/**
 * @author wuxiongbin
 */
public class StreamFrame extends DefaultByteBufHolder {

    public long dwTime;
    public boolean bIsKey;
    public int streamType;

    public StreamFrame(ByteBuf buf) {
        super(buf);
    }
}
