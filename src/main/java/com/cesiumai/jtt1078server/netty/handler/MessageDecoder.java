package com.cesiumai.jtt1078server.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author wuxiongbin
 */
@Slf4j
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int LengthOffsetPosition = 28;
        while (byteBuf.readableBytes() > 0 && (byteBuf.getInt(0) & 0x7fffffff) != 0x30316364) {
            byteBuf.readByte();
        }
        if (byteBuf.readableBytes() < LengthOffsetPosition + 2) {
            return;
        }
        byte b = byteBuf.getByte(15);
        int dataType = (b >> 4) & 0x0f;
        if (dataType == 0b0010) {
            LengthOffsetPosition -= 4;
        }
        if (dataType == 0b0100) {
            LengthOffsetPosition -= 12;
        }
        int bodyLength = byteBuf.getUnsignedShort(LengthOffsetPosition);
        if (bodyLength < 0) {
            byteBuf.readByte();
            return;
        }
        int frameLength = bodyLength + LengthOffsetPosition + 2;
        if (byteBuf.readableBytes() < frameLength) {
            return;
        }
        ByteBuf frame = Unpooled.buffer(frameLength);
        byteBuf.readBytes(frame);
        log.debug("bodyLength ===> {} , frameLength ===> {}", bodyLength, frameLength);
        list.add(DataFrame.parse(frame));
        byteBuf.discardReadBytes();
    }
}
