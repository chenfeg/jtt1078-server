package com.cesiumai.jtt1078server.netty.handler;

import com.cesiumai.jtt1078server.netty.stream.StreamFrame;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wuxiongbin
 */
@Slf4j
public class H264ToFlvEncoder extends MessageToMessageEncoder<StreamFrame> {

    private ByteBuf lastIFrame;
    private boolean isWriteHeader = false;
    private long previousFrameTimestamp = 0;
    private int offSetTimestamp = 0;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, StreamFrame streamFrame, List<Object> list) throws Exception {
        ByteBuf h24frame = streamFrame.content();
        ByteBuf buf = h24frame.copy(4, h24frame.readableBytes() - 4);
        //log.info("buf: {}", Hex.encodeHexString(ByteBufUtil.getBytes(buf)));
        if (previousFrameTimestamp != 0) {
            offSetTimestamp += (streamFrame.dwTime - previousFrameTimestamp);
        }
        //log.info("data stream frame : {}", offSetTimestamp);
        writeFlv(offSetTimestamp, channelHandlerContext, getNetworkAbstractLayerUnit(buf), list);
        previousFrameTimestamp = streamFrame.dwTime;
    }

    private void writeFlv(int nTimestamp, ChannelHandlerContext ctx, List<ByteBuf> layerUnit, List<Object> list) {
        ByteBuf sps = null, pps = null;
        for (ByteBuf nalu : layerUnit) {
            //log.info("nalu: {}", Hex.encodeHexString(ByteBufUtil.getBytes(nalu)));
            int type = nalu.getByte(0) & 0x1f;
            if (type == 7) {
                sps = Unpooled.copiedBuffer(nalu);
            } else if (type == 8) {
                pps = Unpooled.copiedBuffer(nalu);
            } else {
                if (type == 5) {
                    lastIFrame = Unpooled.copiedBuffer(nalu);
                }
                if (isWriteHeader) {
                    writeOtherFlvBody(nTimestamp, ctx, list, Unpooled.copiedBuffer(nalu));
                }
            }
        }
        if (null != sps && null != pps && !isWriteHeader) {
            ByteBuf flvHeader = ctx.alloc().directBuffer();
            flvHeader.writeByte('F');
            flvHeader.writeByte('L');
            flvHeader.writeByte('V');
            flvHeader.writeByte((byte) 0x01);                 // version
            flvHeader.writeByte((byte) 0x01);
            flvHeader.writeInt(0x09);
            int preTagSize = 0x00;
            flvHeader.writeInt(preTagSize);

            int nDataSize = 1 + 1 + 3 + 6 + 2 + sps.readableBytes() + 1 + 2 + pps.readableBytes();
            flvHeader.writeByte((byte) 0x09);
            flvHeader.writeMedium(nDataSize);
            flvHeader.writeMedium(nTimestamp);
            flvHeader.writeByte(nTimestamp >> 24);
            flvHeader.writeZero(3);
            flvHeader.writeByte(0x17);
            flvHeader.writeByte(0x00);
            flvHeader.writeZero(3);
            flvHeader.writeByte(0x01);
            flvHeader.writeBytes(sps, 1, 3);
            flvHeader.writeByte((byte) 0xff);
            flvHeader.writeByte((byte) 0xe1);
            flvHeader.writeShort((short) sps.readableBytes());
            flvHeader.writeBytes(ByteBufUtil.getBytes(sps));
            flvHeader.writeByte((byte) 0x01);
            flvHeader.writeShort((short) pps.readableBytes());
            flvHeader.writeBytes(ByteBufUtil.getBytes(pps));
            preTagSize = 11 + nDataSize;
            flvHeader.writeInt(preTagSize);
            list.add(flvHeader);
            isWriteHeader = true;
            if (lastIFrame != null && lastIFrame.hasArray()) {
                writeOtherFlvBody(nTimestamp, ctx, list, lastIFrame);
                lastIFrame.clear();
            }
        }
    }

    private void writeOtherFlvBody(int nTimestamp, ChannelHandlerContext ctx, List<Object> list, ByteBuf other) {
        int type = other.getByte(0) & 0x1f;
        if (type == 6 || type == 7 || type == 8) return;
        int nDataSize = 1 + 1 + 3 + 4 + other.readableBytes();
        int preTagSize = 11 + nDataSize;
        ByteBuf ordinary = ctx.alloc().directBuffer();
        ordinary.writeByte(0x09);
        ordinary.writeMedium(nDataSize);
        ordinary.writeMedium(nTimestamp);
        ordinary.writeByte(nTimestamp >> 24);
        ordinary.writeZero(3);
        if (type == 5) {
            ordinary.writeByte(0x17);
        } else {
            ordinary.writeByte(0x27);
        }
        ordinary.writeByte(0x01);
        ordinary.writeMedium(0x00);
        ordinary.writeInt(other.readableBytes());
        ordinary.writeBytes(ByteBufUtil.getBytes(other));
        ordinary.writeInt(preTagSize);
        list.add(ordinary);
    }

    private List<ByteBuf> getNetworkAbstractLayerUnit(ByteBuf buf) {
        List<ByteBuf> list = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < buf.readableBytes() - 3; i++) {
            int a = buf.getByte(i) & 0xff;
            int b = buf.getByte(i + 1) & 0xff;
            int c = buf.getByte(i + 2) & 0xff;
            int d = buf.getByte(i + 3) & 0xff;
            if (a == 0x00 && b == 0x00 && c == 0x00 && d == 0x01) {
                if (i == 0) continue;
                ByteBuf byteBuf = Unpooled.buffer(i - index - 4);
                buf.getBytes(index + 4, byteBuf);
                index = i;
                list.add(byteBuf);
            }
        }
        if (buf.readInt() == 1) {
            ByteBuf byteBuf = Unpooled.buffer(buf.readableBytes() - index);
            buf.getBytes(index + 4, byteBuf);
            list.add(byteBuf);
        }
        return list;
    }
}
